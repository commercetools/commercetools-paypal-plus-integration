package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;

/**
 * Handles all actions related to the payment. This class is created
 * tenant-specific and user does not need to provide tenants for every action.
 */
public class PaymentHandler {

    private final CtpFacade ctpFacade;
    private final PaypalPlusFacade paypalPlusFacade;

    public PaymentHandler(@Nonnull CtpFacade ctpFacade,
                          @Nonnull PaypalPlusFacade paypalPlusFacade) {
        this.ctpFacade = ctpFacade;
        this.paypalPlusFacade = paypalPlusFacade;
    }

    public PaymentHandleResult handlePayment(@Nonnull String paymentId){
        //TODO: @andrii.kovalenko
        try {
            ctpFacade.getCartService().getByPaymentId(paymentId);
            // mapping
            return new PaymentHandleResult(HttpStatus.OK);
        } catch (Exception e) {
            return new PaymentHandleResult(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}