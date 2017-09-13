package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Processes PAYMENT.SALE.DENIED event. Change charge state of the corresponding CTP payment to FAILURE
 */
@Component
public class PaymentSaleDeniedProcessor extends PaymentSaleNotificationProcessorBase {

    @Autowired
    public PaymentSaleDeniedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_DENIED;
    }

    @Nonnull
    @Override
    protected TransactionType getExpectedTransactionType() {
        return TransactionType.CHARGE;
    }

    @Nonnull
    @Override
    protected TransactionState getExpectedTransactionState() {
        return TransactionState.FAILURE;
    }
}