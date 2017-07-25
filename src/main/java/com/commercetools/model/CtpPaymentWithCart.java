package com.commercetools.model;


import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.util.CustomFieldUtil.getCustomFieldStringOrEmpty;

/**
 * Class aggregator for payment and related cart object. This object guarantees the payment and the cart are non-null.
 */
public class CtpPaymentWithCart {
    private Payment payment;
    private Cart cart;

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
}
