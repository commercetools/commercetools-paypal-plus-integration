package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Processes event notification of type PAYMENT.SALE.PENDING
 */
@Component
public class PaymentSalePendingProcessor extends PaymentSaleNotificationProcessorBase {

    @Autowired
    public PaymentSalePendingProcessor(@Nonnull Gson gson, @Nonnull PaypalPlusFormatter paypalPlusFormatter) {
        super(gson, paypalPlusFormatter);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_PENDING;
    }

    @Nonnull
    @Override
    protected TransactionType getExpectedTransactionType() {
        return TransactionType.CHARGE;
    }

    @Nonnull
    @Override
    protected TransactionState getExpectedTransactionState() {
        return TransactionState.PENDING;
    }
}