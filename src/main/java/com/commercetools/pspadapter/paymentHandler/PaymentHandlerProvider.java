package com.commercetools.pspadapter.paymentHandler;

import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface PaymentHandlerProvider {

    /**
     * Returns {@link PaymentHandler} by the tenant's name
     */
    Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName);
}