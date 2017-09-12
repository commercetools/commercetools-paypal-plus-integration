package com.commercetools.payment.constants.ctp;

public final class CtpPaymentMethods {

    /**
     * Most of the payment methods, which are selected in Paypal Plus iframe (Credit Card, Paypal, Invoice and so on).
     * Exception: {@link #INSTALLMENT} payment type.
     */
    public static final String DEFAULT = "default";

    /**
     * Installment (de: Ratenzahlung) payment method, which is processed differently from other methods.
     */
    public static final String INSTALLMENT = "installment";

    private CtpPaymentMethods() {
    }
}
