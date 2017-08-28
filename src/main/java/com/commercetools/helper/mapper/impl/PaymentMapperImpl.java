package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.*;
import io.sphere.sdk.cartdiscounts.DiscountedLineItemPriceForQuantity;
import io.sphere.sdk.carts.CustomLineItem;
import io.sphere.sdk.carts.LineItem;
import io.sphere.sdk.models.LocalizedString;
import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
import static com.commercetools.util.MoneyUtil.getActualShippingCost;
import static com.commercetools.util.MoneyUtil.getActualTax;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@Component
public class PaymentMapperImpl implements PaymentMapper {

    private final PaypalPlusFormatter paypalPlusFormatter;

    @Autowired
    public PaymentMapperImpl(@Nonnull PaypalPlusFormatter paypalPlusFormatter) {
        this.paypalPlusFormatter = paypalPlusFormatter;
    }

    @Override
    @Nonnull
    public Payment ctpPaymentToPaypalPlus(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        Payment mappedPayment = new Payment();

        mappedPayment.setIntent(SALE);
        mappedPayment.setPayer(getPayer(paymentWithCartLike));
        mappedPayment.setTransactions(getTransactions(paymentWithCartLike));
        mappedPayment.setRedirectUrls(getRedirectUrls(paymentWithCartLike));

        return mappedPayment;
    }

    @Nonnull
    protected Payer getPayer(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new Payer()
                .setPaymentMethod(paymentWithCartLike.getPaymentMethod());
    }

    @Nonnull
    protected List<Transaction> getTransactions(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return singletonList(getTransaction(paymentWithCartLike));
    }

    @Nonnull
    protected RedirectUrls getRedirectUrls(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new RedirectUrls()
                .setReturnUrl(paymentWithCartLike.getReturnUrl())
                .setCancelUrl(paymentWithCartLike.getCancelUrl());
    }

    @Nonnull
    protected Transaction getTransaction(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        Transaction transaction = new Transaction();
        transaction.setAmount(getTransactionAmount(paymentWithCartLike));
        transaction.setDescription(getTransactionDescription(paymentWithCartLike));
        transaction.setItemList(getTransactionItemList(paymentWithCartLike));
        return transaction;
    }

    @Nonnull
    protected Amount getTransactionAmount(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        MonetaryAmount totalPrice = paymentWithCartLike.getPayment().getAmountPlanned();
        String currencyCode = totalPrice.getCurrency().getCurrencyCode();

        return new Amount()
                .setCurrency(currencyCode)
                .setTotal(paypalPlusFormatter.monetaryAmountToString(totalPrice))
                .setDetails(getTransactionDetails(paymentWithCartLike));
    }

    @Nonnull
    protected String getTransactionDescription(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return "Payment from commercetools Paypal Plus integration service";
    }

    @Nonnull
    protected ItemList getTransactionItemList(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new ItemList()
                .setItems(getLineItems(paymentWithCartLike));
    }

    /**
     * Detalize line items/taxes/shipping cost.
     * <p>
     * <b>Note:</b> if you specify this property in a create payment request, PaypalPlus service will validate the
     * <i>subtotal</i> over total line items costs, this means <b>the line item prices should be specified excluding
     * taxes</b>! Follow <a href="https://www.paypalobjects.com/webstatic/de_DE/downloads/PayPal-PLUS-IntegrationGuide.pdf">
     * Integrating PayPal PLUS</a> guide <i>04.1. Create a payment</i> chapter:
     * <pre>
     * Please note:
     * Including / excluding VAT
     * To avoid rounding errors we recommend not submitting tax amounts on line item basis. Calculated tax amounts for
     * the entire shopping basket may be submitted in the amount objects. In this case the item amounts will be treated
     * as amounts excluding tax. In a B2C scenario, where taxes are included, no taxes should be submitted to PayPal.
     * </pre>
     *
     * @param paymentWithCartLike cart to parse
     * @return subtotal (line items)/taxes/shipping cost details.
     */
    @Nonnull
    protected Details getTransactionDetails(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        MonetaryAmount totalPrice = paymentWithCartLike.getPayment().getAmountPlanned();
        String currencyCode = totalPrice.getCurrency().getCurrencyCode();
        Money ZERO = Money.of(0, currencyCode);

        // Total must be equal to the sum of shipping, tax and subtotal, if they are specified
        MonetaryAmount shipping = getActualShippingCost(paymentWithCartLike.getCart()).orElse(ZERO);
        MonetaryAmount subtotal = totalPrice.subtract(shipping);

        return new Details()
                .setSubtotal(paypalPlusFormatter.monetaryAmountToString(subtotal))
                .setShipping(paypalPlusFormatter.monetaryAmountToString(shipping));
    }

    /**
     * @param paymentWithCartLike cart holder which to map.
     * @return Aggregated list of {@link LineItem} and {@link CustomLineItem} mapped to Paypal Plus {@link Item}.
     */
    @Nonnull
    protected List<Item> getLineItems(@Nonnull CtpPaymentWithCart paymentWithCartLike) {

        final List<Locale> locales = paymentWithCartLike.getLocalesWithDefault();

        Stream<Item> lineItemStream = paymentWithCartLike.getCart().getLineItems().stream()
                .flatMap(lineItem -> mapLineItemToPaypalPlusItem(lineItem, locales));

        Stream<Item> customLineItemStream = paymentWithCartLike.getCart().getCustomLineItems().stream()
                .flatMap(customLineItem -> mapCustomLineItemToPaypalPlusItem(customLineItem, locales));

        return concat(lineItemStream, customLineItemStream)
                .collect(toList());
    }

    /**
     * Map {@link LineItem} properties to {@link Item} instance.
     * <p>
     * <b>Note:</b> The result of the mapping could be one-to-many, if the {@code lineItem} has complex discount applied.
     * In this case it might be split to separate items with different quantity and price. Although keep in mind,
     * the different entries still could have the same price, they just split because they have different discounts
     * applied.
     *
     * @param lineItem line item to map.
     * @param locales  List of {@link Locale} to resolve localized line item properties, like {@link LineItem#getName()}
     * @return stream of single item, if discounts are not applied, or multiple items, if discounts are applied.
     * @see #mapCustomLineItemToPaypalPlusItem(CustomLineItem, List)
     */
    protected Stream<Item> mapLineItemToPaypalPlusItem(@Nonnull LineItem lineItem, @Nonnull List<Locale> locales) {
        if (lineItem.getDiscountedPricePerQuantity().size() > 0) {
            return lineItem.getDiscountedPricePerQuantity().stream()
                    .map(dlipfq -> createPaypalPlusItem(lineItem.getName(), locales, dlipfq))
                    .map(item -> item.setSku(lineItem.getVariant().getSku()));
        }

        MonetaryAmount actualLineItemPrice = lineItem.getPrice().getValue();
        return Stream.of(createPaypalPlusItem(lineItem.getName(), locales, lineItem.getQuantity(), actualLineItemPrice))
                .map(item -> item.setSku(lineItem.getVariant().getSku()));
    }

    /**
     * Similar to {@link #mapLineItemToPaypalPlusItem(LineItem, List)}, but for {@link CustomLineItem}.
     * This entity type has a bit different signature for some properties, like price, name, sku and so on.
     * <p>
     * (Note about the name: unfortunately {@link CustomLineItem#getName()} and {@link LineItem#getName()} are different
     * interface methods, but have the same signature, and common method is not represented in base
     * {@link io.sphere.sdk.carts.LineItemLike} interface, but might be if future)
     *
     * @param customLineItem line item to map
     * @param locales        List of {@link Locale} to resolve localized line item properties,
     *                       like {@link LineItem#getName()}
     * @return stream of single item, if discounts are not applied, or multiple items, if discounts are applied.
     * @see #mapCustomLineItemToPaypalPlusItem(CustomLineItem, List)
     */
    protected Stream<Item> mapCustomLineItemToPaypalPlusItem(@Nonnull CustomLineItem customLineItem, @Nonnull List<Locale> locales) {
        if (customLineItem.getDiscountedPricePerQuantity().size() > 0) {
            return customLineItem.getDiscountedPricePerQuantity().stream()
                    .map(dlipfq -> createPaypalPlusItem(customLineItem.getName(), locales, dlipfq));
        }

        MonetaryAmount actualCustomLineItemPrice = customLineItem.getMoney();
        return Stream.of(createPaypalPlusItem(customLineItem.getName(), locales,
                customLineItem.getQuantity(), actualCustomLineItemPrice));
    }

    protected Item createPaypalPlusItem(@Nonnull LocalizedString itemName, @Nonnull List<Locale> locales,
                                        @Nonnull DiscountedLineItemPriceForQuantity dlipfq) {
        return createPaypalPlusItem(itemName, locales, dlipfq.getQuantity(), dlipfq.getDiscountedPrice().getValue());
    }

    protected Item createPaypalPlusItem(@Nonnull LocalizedString itemName, @Nonnull List<Locale> locales,
                                        @Nonnull Long quantity, @Nonnull MonetaryAmount price) {
        return new Item(itemName.get(locales),
                String.valueOf(quantity),
                paypalPlusFormatter.monetaryAmountToString(price),
                price.getCurrency().getCurrencyCode());
    }
}
