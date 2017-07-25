package com.commercetools.pspadapter.executor;

import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;

import javax.annotation.Nonnull;

/**
 * A wrapper class for all services that communicate with Paypal Plus
 */
public class PaypalPlusExecutor {

    public final PaypalPlusPaymentService paymentService;

    public PaypalPlusExecutor(@Nonnull PaypalPlusPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public PaypalPlusPaymentService getPaymentService() {
        return paymentService;
    }
}