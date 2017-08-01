package com.commercetools.payment.constants.paypalPlus;

public final class PaypalPlusPaymentIntent {

    /**
     * Makes an immediate payment
     */
    public static final String SALE = "sale";

    /**
     * <a href="https://developer.paypal.com/docs/integration/direct/payments/capture-payment/">
     * Authorizes a payment for capture later</a>
     */
    public static final String AUTHORIZE = "authorize";

    /**
     * <a href="https://developer.paypal.com/docs/integration/direct/payments/orders/">
     * Creates an order</a>
     */
    public static final String ORDER = "order";

    private PaypalPlusPaymentIntent() {
    }
}
