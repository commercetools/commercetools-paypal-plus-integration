package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.executor.CtpExecutor;
import com.commercetools.pspadapter.executor.PaypalPlusExecutor;
import org.springframework.http.HttpStatus;

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

    public PaymentHandleResult handlePayment(@Nonnull String paymentId){
        //TODO: @andrii.kovalenko
        try {
            ctpExecutor.getCartService().getByPaymentId(paymentId);
            // mapping
            paypalPlusExecutor.getPaymentService().create(null);
            return new PaymentHandleResult(HttpStatus.OK);
        } catch (Exception e) {
            return new PaymentHandleResult(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}