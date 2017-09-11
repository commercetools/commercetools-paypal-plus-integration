package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Processes event notification of type PAYMENT.SALE.COMPLETED
 */
@Component
public class PaymentSaleCompletedProcessor extends PaymentSaleNotificationProcessorBase {

    @Autowired
    public PaymentSaleCompletedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_COMPLETED;
    }

    @Override
    protected TransactionType getExpectedTransactionType() {
        return TransactionType.CHARGE;
    }

    @Override
    protected TransactionState getExpectedTransactionState() {
        return TransactionState.SUCCESS;
    }
}