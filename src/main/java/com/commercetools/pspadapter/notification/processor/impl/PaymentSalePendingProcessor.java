package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.TransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Processes event notification of type PAYMENT.SALE.PENDING
 */
@Component
public class PaymentSalePendingProcessor extends PaymentSaleSimpleProcessorBase {

    @Autowired
    public PaymentSalePendingProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_PENDING;
    }

    @Override
    List<? extends UpdateAction<Payment>> createUpdateCtpTransactionActions(@Nonnull Payment ctpPayment, @Nonnull Event event) {
        return createUpdateCtpTransactionState(ctpPayment, TransactionState.SUCCESS, TransactionState.PENDING);
    }
}