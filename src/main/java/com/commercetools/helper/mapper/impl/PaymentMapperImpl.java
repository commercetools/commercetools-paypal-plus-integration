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
import javax.annotation.Nullable;
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
    @Nullable
    public Payment ctpPaymentToPaypalPlus(@Nullable CtpPaymentWithCart paymentWithCartLike) {
        if (paymentWithCartLike == null) {
            return null;
        }

        Payment mappedPayment = new Payment();

        mappedPayment.setCart(paymentWithCartLike.getCart().getId());
        mappedPayment.setIntent(SALE);
        mappedPayment.setPayer(getPayer(paymentWithCartLike));
        mappedPayment.setTransactions(getTransactions(paymentWithCartLike));
        mappedPayment.setRedirectUrls(getRedirectUrls(paymentWithCartLike));

        return mappedPayment;
    }

    @Nonnull
    protected Payer getPayer(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new Payer()
                .setFundingInstruments(getFundingInstrumentList(paymentWithCartLike))
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
    protected List<FundingInstrument> getFundingInstrumentList(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return singletonList(new FundingInstrument()
                .setCreditCardToken(getCreditCardToken(paymentWithCartLike)));
    }

    @Nonnull
    protected CreditCardToken getCreditCardToken(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new CreditCardToken(paymentWithCartLike.getCreditCardToken());
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
        Money ZERO = Money.of(0, currencyCode);

        // Total must be equal to the sum of shipping, tax and subtotal, if they are specified
        MonetaryAmount shipping = getActualShippingCost(paymentWithCartLike.getCart()).orElse(ZERO);
        MonetaryAmount tax = getActualTax(paymentWithCartLike.getCart()).orElse(ZERO);
        MonetaryAmount subtotal = totalPrice.subtract(shipping).subtract(tax);

        Details details = new Details()
                .setSubtotal(paypalPlusFormatter.monetaryAmountToString(subtotal))
                .setShipping(paypalPlusFormatter.monetaryAmountToString(shipping))
                .setTax(paypalPlusFormatter.monetaryAmountToString(tax));

        return new Amount()
                .setCurrency(currencyCode)
                .setTotal(paypalPlusFormatter.monetaryAmountToString(totalPrice))
                .setDetails(details);
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
     * @param paymentWithCartLike cart holder which to map.
     * @return Aggregated list of {@link LineItem} and {@link CustomLineItem} mapped to Paypal Plus {@link Item}.
     */
    @Nonnull
    protected List<Item> getLineItems(@Nonnull CtpPaymentWithCart paymentWithCartLike) {

        final Locale locale = paymentWithCartLike.getLocaleOrDefault();

        Stream<Item> lineItemStream = paymentWithCartLike.getCart().getLineItems().stream()
                .flatMap(lineItem -> mapLineItemToPaypalPlusItem(lineItem, locale));

        Stream<Item> customLineItemStream = paymentWithCartLike.getCart().getCustomLineItems().stream()
                .flatMap(customLineItem -> mapCustomLineItemToPaypalPlusItem(customLineItem, locale));

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
     * @param locale   {@link Locale} to resolve localized line item properties, like {@link LineItem#getName()}
     * @return stream of single item, if discounts are not applied, or multiple items, if discounts are applied.
     * @see #mapCustomLineItemToPaypalPlusItem(CustomLineItem, Locale)
     */
    protected Stream<Item> mapLineItemToPaypalPlusItem(@Nonnull LineItem lineItem, @Nonnull Locale locale) {
        if (lineItem.getDiscountedPricePerQuantity().size() > 0) {
            return lineItem.getDiscountedPricePerQuantity().stream()
                    .map(dlipfq -> createPaypalPlusItem(lineItem.getName(), locale, dlipfq))
                    .map(item -> item.setSku(lineItem.getVariant().getSku()));
        }

        MonetaryAmount actualLineItemPrice = lineItem.getPrice().getValue();
        return Stream.of(createPaypalPlusItem(lineItem.getName(), locale, lineItem.getQuantity(), actualLineItemPrice))
                .map(item -> item.setSku(lineItem.getVariant().getSku()));
    }

    /**
     * Similar to {@link #mapLineItemToPaypalPlusItem(LineItem, Locale)}, but for {@link CustomLineItem}.
     * This entity type has a bit different signature for some properties, like price, name, sku and so on.
     * <p>
     * (Note about the name: unfortunately {@link CustomLineItem#getName()} and {@link LineItem#getName()} are different
     * interface methods, but have the same signature, and common method is not represented in base
     * {@link io.sphere.sdk.carts.LineItemLike} interface, but might be if future)
     *
     * @param customLineItem line item to map
     * @param locale         {@link Locale} to resolve localized line item properties, like {@link LineItem#getName()}
     * @return stream of single item, if discounts are not applied, or multiple items, if discounts are applied.
     * @see #mapCustomLineItemToPaypalPlusItem(CustomLineItem, Locale)
     */
    protected Stream<Item> mapCustomLineItemToPaypalPlusItem(@Nonnull CustomLineItem customLineItem, @Nonnull Locale locale) {
        if (customLineItem.getDiscountedPricePerQuantity().size() > 0) {
            return customLineItem.getDiscountedPricePerQuantity().stream()
                    .map(dlipfq -> createPaypalPlusItem(customLineItem.getName(), locale, dlipfq));
        }

        MonetaryAmount actualCustomLineItemPrice = customLineItem.getMoney();
        return Stream.of(createPaypalPlusItem(customLineItem.getName(), locale,
                customLineItem.getQuantity(), actualCustomLineItemPrice));
    }

    protected Item createPaypalPlusItem(@Nonnull LocalizedString itemName, @Nonnull Locale locale,
                                        @Nonnull DiscountedLineItemPriceForQuantity dlipfq) {
        return createPaypalPlusItem(itemName, locale, dlipfq.getQuantity(), dlipfq.getDiscountedPrice().getValue());
    }

    protected Item createPaypalPlusItem(@Nonnull LocalizedString itemName, @Nonnull Locale locale,
                                        @Nonnull Long quantity, @Nonnull MonetaryAmount price) {
        return new Item(itemName.get(locale),
                String.valueOf(quantity),
                paypalPlusFormatter.monetaryAmountToString(price),
                price.getCurrency().getCurrencyCode());
    }
}
