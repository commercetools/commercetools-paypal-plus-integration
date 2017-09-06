package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.commercetools.util.CtpPaymentUtil.findTransactionByTypeAndState;

/**
 * Processes PAYMENT.SALE.DENIED event. Change charge state of the corresponding CTP payment to FAILURE
 */
@Component
public class PaymentSaleDeniedProcessor extends NotificationProcessorBase {

    @Autowired
    public PaymentSaleDeniedProcessor(@Nonnull Gson gson) {
        super(gson);
    }

    @Override
    List<? extends UpdateAction<Payment>> createUpdateCtpTransactionActions(@Nonnull Payment ctpPayment, @Nonnull Event event) {
        Optional<Transaction> txnOpt = findTransactionByTypeAndState(ctpPayment.getTransactions(), TransactionType.CHARGE, TransactionState.PENDING);
        return txnOpt
                .map(txn -> Collections.singletonList(ChangeTransactionState.of(TransactionState.FAILURE, txn.getId())))
                .orElse(Collections.emptyList());
    }

    @Override
    public boolean canProcess(@Nonnull Event event) {
        return NotificationEventType.PAYMENT_SALE_DENIED.getPaypalEventTypeName()
                .equalsIgnoreCase(event.getEventType());
    }
}