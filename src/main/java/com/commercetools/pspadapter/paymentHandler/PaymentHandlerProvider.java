package com.commercetools.pspadapter.paymentHandler;

import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;

import javax.annotation.Nonnull;

public interface PaymentHandlerProvider {

    /**
     * Returns {@link PaymentHandler} by the tenant's name
     */
    PaymentHandler getPaymentHandler(@Nonnull String tenantName);
}