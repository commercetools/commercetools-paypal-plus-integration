package com.commercetools.service.main;

import com.commercetools.service.main.impl.PaymentHandler;

/**
 * TODO: add class description
 */
public interface PaymentHandlerProvider {

    PaymentHandler getPaymentHandler(String tenantName);
}