package com.commercetools.pspadapter.notification.processor.impl;

import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.mockito.invocation.InvocationOnMock;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseNotificationTest {

    protected Payment createMockPayment(String interactionId, TransactionType txnType, TransactionState txnState) {
        Payment ctpMockPayment = mock(Payment.class);
        Transaction transaction = mock(Transaction.class);
        when(ctpMockPayment.getTransactions()).thenReturn(Collections.singletonList(transaction));
        when(transaction.getType()).thenReturn(txnType);
        when(transaction.getState()).thenReturn(txnState);
        when(transaction.getInteractionId()).thenReturn(interactionId);
        return ctpMockPayment;
    }

    @SuppressWarnings("unchecked")
    protected void verifyUpdatePaymentCall(Payment ctpMockPayment, InvocationOnMock invocation, TransactionState state) {
        Payment payment = invocation.getArgument(0, Payment.class);
        List<UpdateAction<Payment>> updateActions = invocation.getArgument(1, List.class);
        assertThat(payment).isEqualTo(ctpMockPayment);
        assertThat(updateActions.size()).isEqualTo(2);
        // One of the action is AddInterfaceInteraction, which is common for all notification processors.
        // I already covered this case in {PaymentSalePendingProcessorTest},
        // so it's not necessary to repeat it here.
        ChangeTransactionState changeTransactionState = (ChangeTransactionState) updateActions.get(1);
        assertThat(changeTransactionState.getState()).isEqualTo(state);
    }
}
