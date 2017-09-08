package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Processes PAYMENT.SALE.REFUNDED event. Updates CTP payment with a new refund transaction.
 */
@Component
public class PaymentSaleRefundedProcessor extends PaymentSaleReturnProcessorBase {

    @Autowired
    public PaymentSaleRefundedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    @Nonnull
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_REFUNDED;
    }

    @Override
    List<? extends UpdateAction<Payment>> createUpdateCtpTransactionActions(@Nonnull Payment ctpPayment, @Nonnull Event event) {
        return createUpdateCtpTransactionActions(ctpPayment, event, TransactionType.REFUND);
    }

}