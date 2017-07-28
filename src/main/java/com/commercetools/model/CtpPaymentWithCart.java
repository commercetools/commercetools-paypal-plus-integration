package com.commercetools.model;


import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Optional;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.util.CustomFieldUtil.getCustomFieldStringOrEmpty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

/**
 * Class aggregator for payment and related cart object. This object guarantees the payment and the cart are non-null.
 */
public class CtpPaymentWithCart {
    private final Payment payment;
    private final Cart cart;

    public CtpPaymentWithCart(@Nonnull Payment payment, @Nonnull Cart cart) {
        this.payment = payment;
        this.cart = cart;
    }

    @Nonnull
    public Payment getPayment() {
        return payment;
    }

    @Nonnull
    public Cart getCart() {
        return cart;
    }

    /**
     * Specific payment method name, like credit card, paypal, vorkasse and so on.
     * <p>
     * See {@link com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods}
     *
     * @return payment method name from {@link Payment#getPaymentMethodInfo()}
     */
    @Nonnull
    public String getPaymentMethod() {
        return Optional.of(payment)
                .map(Payment::getPaymentMethodInfo)
                .map(PaymentMethodInfo::getMethod)
                .orElse("");
    }

    /**
     * @return Credit card token value from
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#CREDIT_CARD_TOKEN} custom field, if exists.
     * Otherwise return empty string.
     * @deprecated looks like should be avoided for Paypal Plus payment, cos hidden in iframe
     */
    @Nonnull
    public String getCreditCardToken() {
        return getCustomFieldStringOrEmpty(payment, CREDIT_CARD_TOKEN);
    }

    /**
     * @return Return URL (success URL) value from
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#SUCCESS_URL_FIELD} custom field, if exists.
     * Otherwise return empty string.
     */
    @Nonnull
    public String getReturnUrl() {
        return getCustomFieldStringOrEmpty(payment, SUCCESS_URL_FIELD);
    }

    /**
     * @return Cancel URL value from
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#CANCEL_URL_FIELD} custom field, if exists.
     * Otherwise return empty string.
     */
    @Nonnull
    public String getCancelUrl() {
        return getCustomFieldStringOrEmpty(payment, CANCEL_URL_FIELD);
    }

    /**
     * Fetch locale from:<ol>
     * <li>payment custom field
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#LANGUAGE_CODE_FIELD}</li>
     * <li>otherwise try to get from {@link Cart#getLocale()}</li>
     * <li>otherwise fallback to default {@link com.commercetools.payment.constants.LocaleConstants#DEFAULT_LOCALE}</li>
     * </ol>
     *
     * @return Locale from payment, or cart, or fallback
     * {@link com.commercetools.payment.constants.LocaleConstants#DEFAULT_LOCALE}
     */
    @Nonnull
    public Locale getLocaleOrDefault() {
        return of(getCustomFieldStringOrEmpty(payment, LANGUAGE_CODE_FIELD))
                .filter(StringUtils::isNotBlank)
                .map(Locale::forLanguageTag)
                .orElseGet(() -> ofNullable(cart.getLocale()).orElse(DEFAULT_LOCALE));
    }
}
