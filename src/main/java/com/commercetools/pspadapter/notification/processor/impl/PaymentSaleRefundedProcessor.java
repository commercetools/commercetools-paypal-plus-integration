package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.TransactionDraft;
import io.sphere.sdk.payments.TransactionDraftBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.*;
import static com.commercetools.util.TimeUtil.toZonedDateTime;

/**
 * Processes PAYMENT.SALE.REFUNDED event. Updates CTP payment with a new refund transaction.
 */
@Component
public class PaymentSaleRefundedProcessor extends PaymentSaleNotificationProcessor {

    private final static Logger logger = LoggerFactory.getLogger(PaymentSaleRefundedProcessor.class);


    @Autowired
    public PaymentSaleRefundedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    public NotificationEventType getNotificationEventType() {
        return NotificationEventType.PAYMENT_SALE_REFUNDED;
    }

    @Override
    List<? extends UpdateAction<Payment>> createUpdateCtpTransactionActions(@Nonnull Payment ctpPayment, @Nonnull Event event) {
        try {
            return Collections.singletonList(createAddTransactionAction(event, TransactionType.REFUND));
        } catch (Throwable t) {
            logger.error("Error when create update actions for eventId={}, ctpPaymentId={}", event.getId(), ctpPayment.getId(), t);
            return Collections.emptyList();
        }
    }

}