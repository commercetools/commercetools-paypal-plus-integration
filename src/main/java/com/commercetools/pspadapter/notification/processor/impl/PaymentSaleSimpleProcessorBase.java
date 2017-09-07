package com.commercetools.pspadapter.notification.processor.impl;

import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;

import javax.annotation.Nonnull;
import java.util.List;

import static com.commercetools.util.CtpPaymentUtil.findTransactionByTypeAndState;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public abstract class PaymentSaleSimpleProcessorBase extends PaymentSaleNotificationProcessorBase {
    public PaymentSaleSimpleProcessorBase(@Nonnull Gson gson) {
        super(gson);
    }

    List<? extends UpdateAction<Payment>> createUpdateCtpTransactionState(@Nonnull Payment ctpPayment,
                                                                          @Nonnull TransactionState fromTransactionState,
                                                                          @Nonnull TransactionState toTransactionState) {
        return findTransactionByTypeAndState(ctpPayment.getTransactions(), TransactionType.CHARGE, fromTransactionState)
                .map(txn -> singletonList(ChangeTransactionState.of(toTransactionState, txn.getId())))
                .orElse(emptyList());
    }
}
