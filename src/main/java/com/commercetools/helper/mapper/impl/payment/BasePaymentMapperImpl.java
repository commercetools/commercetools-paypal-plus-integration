package com.commercetools.helper.mapper.impl.payment;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.AddressMapper;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping;
import com.paypal.api.ApplicationContext;
import com.paypal.api.payments.*;
import io.sphere.sdk.cartdiscounts.DiscountedLineItemPriceForQuantity;
import io.sphere.sdk.carts.CustomLineItem;
import io.sphere.sdk.carts.LineItem;
import io.sphere.sdk.models.LocalizedString;
import org.javamoney.moneta.Money;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
import static com.commercetools.util.MoneyUtil.getActualShippingCost;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Basic common ctp->pp payment properties mapping.
 */
public abstract class BasePaymentMapperImpl implements PaymentMapper {

    protected final PaypalPlusFormatter paypalPlusFormatter;
    protected final CtpToPaypalPlusPaymentMethodsMapping ctpToPpPaymentMethodsMapping;
    protected final AddressMapper addressMapper;

    public BasePaymentMapperImpl(@Nonnull PaypalPlusFormatter paypalPlusFormatter,
                                 @Nonnull CtpToPaypalPlusPaymentMethodsMapping ctpToPpPaymentMethodsMapping,
                                 @Nonnull AddressMapper addressMapper) {
        this.paypalPlusFormatter = paypalPlusFormatter;
        this.ctpToPpPaymentMethodsMapping = ctpToPpPaymentMethodsMapping;
        this.addressMapper = addressMapper;
    }

    @Override
    @Nonnull
    public Payment ctpPaymentToPaypalPlus(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        // while PayPal Plus SDK developers are fixing issue
        // href="https://github.com/paypal/PayPal-Java-SDK/issues/330
        // we use this extended payment type to set application context with shipping preference
        PaymentEx mappedPayment = new PaymentEx();

        mappedPayment.setApplicationContext(getApplicationContext(paymentWithCartLike));

        mappedPayment.setIntent(SALE);
        mappedPayment.setPayer(getPayer(paymentWithCartLike));
        mappedPayment.setTransactions(getTransactions(paymentWithCartLike));
        mappedPayment.setRedirectUrls(getRedirectUrls(paymentWithCartLike));
        mappedPayment.setExperienceProfileId(getExperienceProfileId(paymentWithCartLike));

        return mappedPayment;
    }

    @Nonnull
    @Override
    public CtpToPaypalPlusPaymentMethodsMapping getCtpToPpPaymentMethodsMapping() {
        return ctpToPpPaymentMethodsMapping;
    }

    @Nonnull
    @Override
    public AddressMapper getAddressMapper() {
        return addressMapper;
    }

    @Nonnull
    protected Payer getPayer(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new Payer()
                .setPaymentMethod(getCtpToPpPaymentMethodsMapping().getPpMethodName())
                .setExternalSelectedFundingInstrumentType(getExternalSelectedFundingInstrumentType(paymentWithCartLike))
                .setPayerInfo(getPayerInfo(paymentWithCartLike));
    }

    @Nullable
    protected String getExternalSelectedFundingInstrumentType(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return null;
    }

    @Nullable
    protected PayerInfo getPayerInfo(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return null;
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

    @Nullable
    protected String getExperienceProfileId(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return paymentWithCartLike.getExperienceProfileId();
    }

    @Nullable
    protected ApplicationContext getApplicationContext(@Nonnull CtpPaymentWithCart paymentWithCart) {
        return new ApplicationContext()
                .setShippingPreference(paymentWithCart.getShippingPreference());
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
        return paymentWithCartLike.getTransactionDescription();
    }

    @Nonnull
    protected ItemList getTransactionItemList(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new ItemList()
                .setItems(getLineItems(paymentWithCartLike))
                .setShippingAddress(getItemListShippingAddress(paymentWithCartLike));
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

    /**
     * As discussed with PayPal Plus support, by default shipping address should not be specified
     * when payment is created, but only patched right before redirecting customer to the approval page:
     * <pre>
     *  It is not allowed to provide customer information with the create payment.
     *  Possibly the customer doesn't use PayPal but for example ‘Vorkasse or Nachnahme’.
     *  This is the reason for the patch call and the handling of customer information.
     * </pre>
     * Custom payment mapper implementations, like
     * {@link InstallmentPaymentMapperImpl#getItemListShippingAddress(CtpPaymentWithCart)}
     * could have some other requirements.
     *
     * @param ctpPaymentWithCart cart holder from which to map address
     * @return shipping address if allowed and available in the {@code ctpPaymentWithCart}, otherwise <b>null</b>
     */
    @Nullable
    protected ShippingAddress getItemListShippingAddress(@Nonnull CtpPaymentWithCart ctpPaymentWithCart) {
        return null;
    }
}
