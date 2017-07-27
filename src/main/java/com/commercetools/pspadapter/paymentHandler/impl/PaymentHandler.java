package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;

import static com.commercetools.pspadapter.tenant.TenantLoggerUtil.createLoggerName;

/**
 * Handles all actions related to the payment. This class is created
 * tenant-specific and user does not need to provide tenants for every action.
 */
public class PaymentHandler {

    private final CtpFacade ctpFacade;
    private final PaypalPlusFacade paypalPlusFacade;

    private final Logger logger;

    public PaymentHandler(@Nonnull CtpFacade ctpFacade,
                          @Nonnull PaypalPlusFacade paypalPlusFacade,
                          @Nonnull String tenantName) {
        this.ctpFacade = ctpFacade;
        this.paypalPlusFacade = paypalPlusFacade;
        logger = LoggerFactory.getLogger(createLoggerName(PaymentHandler.class, tenantName));
    }

    public PaymentHandleResult handlePayment(@Nonnull String paymentId){
        //TODO: @andrii.kovalenko
        try {
            ctpFacade.getCartService().getByPaymentId(paymentId);
            // mapping
            return new PaymentHandleResult(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", paymentId, e);
            return new PaymentHandleResult(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}