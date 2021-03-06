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
 * Processes PAYMENT.SALE.REFUNDED event. Updates CTP payment with a new refund transaction.
 */
@Component
public class PaymentSaleRefundedProcessor extends PaymentSaleNotificationProcessorBase {

    @Autowired
    public PaymentSaleRefundedProcessor(@Nonnull Gson gson, @Nonnull PaypalPlusFormatter paypalPlusFormatter) {
        super(gson, paypalPlusFormatter);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_REFUNDED;
    }

    @Nonnull
    @Override
    protected TransactionType getCtpTransactionType() {
        return TransactionType.REFUND;
    }

    @Nonnull
    @Override
    protected TransactionState getCtpTransactionState() {
        return TransactionState.SUCCESS;
    }
}