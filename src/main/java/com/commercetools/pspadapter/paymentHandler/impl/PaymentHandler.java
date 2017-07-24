package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.executor.CtpExecutor;
import com.commercetools.pspadapter.executor.PaypalPlusExecutor;

import javax.annotation.Nonnull;

/**
 * Handles all actions related to the payment. This class is created
 * tenant-specific and user does not need to provide tenants for every action.
 */
public class PaymentHandler {

    private final CtpExecutor ctpExecutor;
    private final PaypalPlusExecutor paypalPlusExecutor;

    public PaymentHandler(@Nonnull CtpExecutor ctpExecutor,
                          @Nonnull PaypalPlusExecutor paypalPlusExecutor) {
        this.ctpExecutor = ctpExecutor;
        this.paypalPlusExecutor = paypalPlusExecutor;
    }

    public void handlePayment(@Nonnull String paymentId){
        ctpExecutor.getCartService().getByPaymentId(paymentId);
        // mapping
        paypalPlusExecutor.getPaymentService().create(null);
    }
}