package com.commercetools.testUtil.ctpUtil;

import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.LocaleConstants;
import com.commercetools.payment.constants.ctp.CtpPaymentCustomFields;
import com.commercetools.payment.constants.ctp.CtpPaymentMethods;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.testUtil.ResourcesUtil;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.CustomLineItemDraft;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.types.CustomFieldsDraftBuilder;
import org.javamoney.moneta.Money;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static io.sphere.sdk.json.SphereJsonUtils.readObjectFromResource;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

/**
 * Utility to create CT platform object instances.
 */
public class CtpResourcesUtil extends ResourcesUtil {

    public static CartDraft getCartDraftWithCustomLineItems(TaxCategory taxCategory) {
        CustomLineItemDraft testItem = CustomLineItemDraft.of(
                LocalizedString.of(Locale.GERMANY, "testItem"),
                "/test",
                Money.of(1, EUR),
                taxCategory,
                10);

        return getCartDraft(singletonList(testItem));
    }

    public static CartDraft getCartDraft(List<CustomLineItemDraft> customLineItemsDrafts) {
        return CartDraftBuilder.of(getCartDraft()).customLineItems(customLineItemsDrafts).build();
    }

    private static CartDraft getCartDraft() {
        return getCartDraftFromResource(resolveMockDataResource("cartDraft.json"));
    }

    public static Payment getDummyPaymentForComplexCartWithDiscounts() {
        return getPaymentFromResource(resolveMockDataResource("paymentMapper/dummyPaymentForComplexCartWithDiscounts.json"));
    }

    public static Payment getDummyPaymentForComplexCartWithoutDiscounts() {
        return getPaymentFromResource(resolveMockDataResource("paymentMapper/dummyPaymentForComplexCartWithoutDiscounts.json"));
    }

    public static Cart getDummyComplexCartWithDiscounts() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithDiscounts.json"));
    }

    /**
     * @return Cart with discounts different shipping and billing addresses.
     */
    public static Cart getDummyComplexCartWithDiscountsShippingBillingAddress() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithDiscountsShippingBillingAddress.json"));
    }

    public static Cart getDummyComplexCartWithoutDiscounts() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithoutDiscounts.json"));
    }

    /**
     * @return Cart without discounts and with different shipping and billing addresses.
     */
    public static Cart getDummyComplexCartWithoutDiscountsShippingBillingAddress() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithoutDiscountsShippingBillingAddress.json"));
    }

    public static CartDraft getDummyComplexCartDraftWithDiscounts() {
        return getCartDraftFromResource(resolveMockDataResource("paymentHandler/dummyCartDraftWithDiscounts.json"));
    }

    public static CtpPaymentWithCart getPaymentWithCart_complexAndDiscount() {
        return new CtpPaymentWithCart(getDummyPaymentForComplexCartWithDiscounts(), getDummyComplexCartWithDiscounts());
    }

    public static CtpPaymentWithCart getPaymentWithCart_complexWithoutDiscount() {
        return new CtpPaymentWithCart(getDummyPaymentForComplexCartWithoutDiscounts(), getDummyComplexCartWithoutDiscounts());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Common util methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Payment getPaymentFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, Payment.class);
    }

    public static Cart getCartFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, Cart.class);
    }

    public static CartDraft getCartDraftFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, CartDraft.class);
    }

    public static PaymentDraftBuilder createPaymentDraftBuilder(@Nonnull MonetaryAmount totalPrice, @Nullable Locale locale) {
        return PaymentDraftBuilder.of(totalPrice)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS).method(CtpPaymentMethods.DEFAULT).build())
                .custom(CustomFieldsDraftBuilder.ofTypeKey("payment-paypal")
                        .addObject(CtpPaymentCustomFields.SUCCESS_URL_FIELD, "http://example.com/success/23456789")
                        .addObject(CtpPaymentCustomFields.CANCEL_URL_FIELD, "http://example.com/cancel/23456789")
                        .addObject(CtpPaymentCustomFields.REFERENCE, "23456789")
                        .addObject(CtpPaymentCustomFields.LANGUAGE_CODE_FIELD, ofNullable(locale).orElse(LocaleConstants.DEFAULT_LOCALE).getLanguage())
                        .build());
    }

    public static CartDraftBuilder createCartDraftBuilder() {
        return CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
                .currency(EUR);
    }

    public static String createCartAndPayment(SphereClient sphereClient) {
        Cart updatedCart = executeBlocking(createCartCS(sphereClient)
                .thenCompose(cart -> createPaymentCS(cart.getTotalPrice(), cart.getLocale(), sphereClient)
                        .thenApply(payment -> new CtpPaymentWithCart(payment, cart))
                        .thenCompose(ctpPaymentWithCart -> sphereClient.execute(CartUpdateCommand.of(ctpPaymentWithCart.getCart(),
                                AddPayment.of(ctpPaymentWithCart.getPayment()))))));

        return updatedCart.getPaymentInfo().getPayments().get(0).getId();
    }

    public static CompletionStage<Cart> createCartCS(SphereClient sphereClient) {
        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
                .currency(EUR)
                .build();
        return sphereClient.execute(CartCreateCommand.of(dummyComplexCartWithDiscounts));
    }

    public static CompletionStage<Payment> createPaymentCS(@Nonnull MonetaryAmount totalPrice,
                                                           Locale locale,
                                                           SphereClient sphereClient) {
        PaymentDraftDsl dsl = createPaymentDraftBuilder(totalPrice, locale)
                .build();
        return sphereClient.execute(PaymentCreateCommand.of(dsl));
    }
}