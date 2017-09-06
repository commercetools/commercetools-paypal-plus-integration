package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

public class PaymentSaleDeniedProcessorTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallUpdatePaymentWithCorrectArgs() {
        // set up
        Payment ctpMockPayment = mock(Payment.class);
        Transaction transaction = mock(Transaction.class);
        when(ctpMockPayment.getTransactions()).thenReturn(Collections.singletonList(transaction));
        when(transaction.getState()).thenReturn(TransactionState.PENDING);
        when(transaction.getType()).thenReturn(TransactionType.CHARGE);

        NotificationProcessorBase processorBase = spy(new PaymentSaleDeniedProcessor(new GsonBuilder().create()));

        doReturn(CompletableFuture.completedFuture(Optional.of(ctpMockPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
        SphereClient sphereClient = mock(SphereClient.class);

        PaymentService paymentService = spy(new PaymentServiceImpl(sphereClient));

        CtpFacade ctpFacade = spy(
                new CtpFacade(mock(CartService.class), mock(OrderService.class), paymentService)
        );

        Event event = new Event();
        event.setEventType(NotificationEventType.PAYMENT_SALE_DENIED.toString());

        // test
        doAnswer(invocation -> {
            Payment payment = invocation.getArgumentAt(0, Payment.class);
            List<UpdateAction<Payment>> updateActions = invocation.getArgumentAt(1, List.class);
            assertThat(payment).isEqualTo(ctpMockPayment);
            assertThat(updateActions.size()).isEqualTo(2);
            ChangeTransactionState changeTransactionState = (ChangeTransactionState) updateActions.get(1);
            assertThat(changeTransactionState.getState()).isEqualTo(TransactionState.FAILURE);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        Payment returnedPayment = processorBase.processEventNotification(ctpFacade, event)
                .toCompletableFuture()
                .join();

        // assert
        assertThat(returnedPayment).isEqualTo(ctpMockPayment);
        verify(paymentService, times(1))
                .updatePayment(any(Payment.class), anyList());
    }
}