package com.commercetools.testUtil.ctpUtil;

import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.CustomLineItemDraft;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.types.CustomFieldsDraftBuilder;
import org.javamoney.moneta.Money;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Locale;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.testUtil.ResourcesUtil.resolveMockDataResource;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getCartDraftFromResource;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

/**
 * Read mock data from the JSON resources for the integration tests.
 */
public final class IntegrationCtpResourcesUtil {

    public static CartDraft getCartDraftWithCustomLineItems(TaxCategory taxCategory) {
        CustomLineItemDraft testItem = CustomLineItemDraft.of(
                LocalizedString.of(Locale.GERMANY, "testItem"),
                "/test",
                Money.of(1, EUR),
                taxCategory,
                10);

        return getCartDraft(singletonList(testItem));
    }

    public static PaymentDraftBuilder createPaymentDraftBuilder(@Nonnull MonetaryAmount totalPrice, @Nullable Locale locale) {
        return PaymentDraftBuilder.of(totalPrice)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface(PAYPAL_PLUS).method(DEFAULT).build())
                .custom(CustomFieldsDraftBuilder.ofTypeKey("payment-paypal")
                        .addObject(SUCCESS_URL_FIELD, "http://example.com/success/23456789")
                        .addObject(CANCEL_URL_FIELD, "http://example.com/cancel/23456789")
                        .addObject(REFERENCE, "23456789")
                        .addObject(LANGUAGE_CODE_FIELD, ofNullable(locale).orElse(DEFAULT_LOCALE).getLanguage())
                        .build());
    }

    public static CartDraft getCartDraft(List<CustomLineItemDraft> customLineItemsDrafts) {
        return CartDraftBuilder.of(getCartDraft()).customLineItems(customLineItemsDrafts).build();
    }

    private static CartDraft getCartDraft() {
        return getCartDraftFromResource(resolveMockDataResource("cartDraft.json"));
    }

    public static CartDraft getDummyComplexCartDraftWithDiscounts() {
        return getCartDraftFromResource(resolveMockDataResource("paymentHandler/dummyCartDraftWithDiscounts.json"));
    }

    private IntegrationCtpResourcesUtil() {
    }
}
