package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Processes event notification of type PAYMENT.SALE.COMPLETED
 */
@Component
public class PaymentSaleCompletedProcessor extends NotificationProcessorBase {

    @Autowired
    public PaymentSaleCompletedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    public boolean canProcess(@Nonnull Event event) {
        return NotificationEventType.PAYMENT_SALE_COMPLETED.getPaypalEventTypeName()
                .equalsIgnoreCase(event.getEventType());
    }

    @Override
    Optional<ChangeTransactionState> createChangeTransactionState(@Nonnull Payment ctpPayment) {
        Optional<Transaction> txnOpt = findMatchingTxn(ctpPayment.getTransactions(), TransactionType.CHARGE, TransactionState.PENDING);
        return txnOpt.map(txn -> ChangeTransactionState.of(TransactionState.SUCCESS, txn.getId()));
    }
}