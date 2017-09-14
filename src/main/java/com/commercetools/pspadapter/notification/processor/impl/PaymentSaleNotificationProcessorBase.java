package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionDraft;
import io.sphere.sdk.payments.TransactionDraftBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.*;
import static com.commercetools.util.CtpPaymentUtil.findTransactionByInteractionId;
import static com.commercetools.util.TimeUtil.toZonedDateTime;

/**
 * Base class for all Payment Sale notification processors
 */
public abstract class PaymentSaleNotificationProcessorBase extends NotificationProcessorBase {

    private final static Logger logger = LoggerFactory.getLogger(PaymentSaleNotificationProcessorBase.class);

    PaymentSaleNotificationProcessorBase(Gson gson) {
        super(gson);
    }

    @Nonnull
    public abstract NotificationEventType getNotificationEventType();

    @Override
    public boolean canProcess(@Nonnull Event event) {
        return getNotificationEventType().getPaypalEventTypeName()
                .equalsIgnoreCase(event.getEventType());
    }

    protected List<UpdateAction<Payment>> createUpdatePaymentActions(@Nonnull Payment ctpPayment, @Nonnull Event event) {
        String resourceId = getResourceId(event);
        return findTransactionByInteractionId(ctpPayment.getTransactions(), resourceId)
                .map(this::processNotificationForTransaction)
                .orElseGet(() -> createAddTransactionActionList(event, getCtpTransactionType()));
    }

    private List<UpdateAction<Payment>> processNotificationForTransaction(@Nonnull Transaction txn) {
        if (isTxnAlreadyUpdated(txn)) {
            // can't do Collections.emptyList() here because map() and generics
            // together generates unexpected return results
            return Collections.emptyList();
        } else {
            return createChangeTransactionStateActionList(txn);
        }
    }

    @Nonnull
    abstract protected TransactionType getCtpTransactionType();

    @Nonnull
    abstract protected TransactionState getCtpTransactionState();

    protected List<UpdateAction<Payment>> createChangeTransactionStateActionList(Transaction txn) {
        return Collections.singletonList(ChangeTransactionState.of(getCtpTransactionState(), txn.getId()));
    }

    protected List<UpdateAction<Payment>> createAddTransactionActionList(@Nonnull Event event,
                                                                         @Nonnull TransactionType transactionType) {
        try {
            Map resource = (Map) event.getResource();
            String resourceId = (String) resource.get(ID);
            Map amount = (Map) resource.get(AMOUNT);
            BigDecimal total = new BigDecimal((String) amount.get(TOTAL));
            String currencyCode = (String) amount.get(CURRENCY);
            String createTime = (String) resource.get(CREATE_TIME);

            TransactionDraft transactionDraft = TransactionDraftBuilder
                    .of(transactionType, Money.of(total, currencyCode))
                    .timestamp(toZonedDateTime(createTime))
                    .interactionId(resourceId)
                    .state(getCtpTransactionState())
                    .build();
            return Collections.singletonList(AddTransaction.of(transactionDraft));
        } catch (Throwable e) {
            logger.error("Unexpected error while creating addTransactionActions " +
                    "for transactionType=[%s] and for Paypal Plus event=[%s]", transactionType, event, e);
            return Collections.emptyList();
        }
    }

    protected String getResourceId(@Nonnull Event event) {
        Map resource = (Map) event.getResource();
        return (String) resource.get(ID);
    }

    private boolean isTxnAlreadyUpdated(Transaction txn) {
        return txn.getState().equals(getCtpTransactionState());
    }
}