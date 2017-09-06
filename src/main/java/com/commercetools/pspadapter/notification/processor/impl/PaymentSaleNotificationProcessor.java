package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.TransactionDraft;
import io.sphere.sdk.payments.TransactionDraftBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.*;
import static com.commercetools.util.TimeUtil.toZonedDateTime;

/**
 * Base class for all Payment Sale notification processors
 */
public abstract class PaymentSaleNotificationProcessor extends NotificationProcessorBase {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentSaleNotificationProcessor.class);

    PaymentSaleNotificationProcessor(Gson gson) {
        super(gson);
    }

    public abstract NotificationEventType getNotificationEventType();

    @Override
    public boolean canProcess(@Nonnull Event event) {
        return getNotificationEventType().getPaypalEventTypeName()
                .equalsIgnoreCase(event.getEventType());
    }

    protected AddTransaction createAddTransactionAction(@Nonnull Event event,
                                                        @Nonnull TransactionType transactionType) {
        Map resource = (Map) event.getResource();
        Map amount = (Map) resource.get(AMOUNT);
        BigDecimal total = new BigDecimal((String) amount.get(TOTAL));
        String currencyCode = (String) amount.get(CURRENCY);
        String createTime = (String) resource.get(CREATE_TIME);

        TransactionDraft transactionDraft = TransactionDraftBuilder
                .of(transactionType, Money.of(total, currencyCode))
                .timestamp(toZonedDateTime(createTime))
                .state(TransactionState.SUCCESS)
                .build();
        return AddTransaction.of(transactionDraft);
    }
}