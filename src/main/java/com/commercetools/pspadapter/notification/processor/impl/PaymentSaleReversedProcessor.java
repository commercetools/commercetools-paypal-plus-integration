package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Processes PAYMENT.SALE.REVERSED event. Updates CTP payment with a new chargeback transaction.
 */
@Component
public class PaymentSaleReversedProcessor extends PaymentSaleNotificationProcessorBase {

    @Autowired
    public PaymentSaleReversedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_REVERSED;
    }

    @Override
    protected TransactionType getExpectedTransactionType() {
        return TransactionType.CHARGEBACK;
    }

    @Override
    protected TransactionState getExpectedTransactionState() {
        return TransactionState.SUCCESS;
    }
}