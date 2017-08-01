package com.commercetools.pspadapter.facade;

import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;

import javax.annotation.Nonnull;

/**
 * A wrapper class for all services that communicate with Paypal Plus
 */
public class PaypalPlusFacade {

    private final PaypalPlusPaymentService paymentService;

    public PaypalPlusFacade(@Nonnull PaypalPlusPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public PaypalPlusPaymentService getPaymentService() {
        return paymentService;
    }
}