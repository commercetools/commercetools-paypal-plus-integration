package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventType.PAYMENT_SALE_COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentSaleCompletedProcessorTest {

    private final Gson gson = new GsonBuilder().create();

    @Test
    public void shouldRecognizeIfEventIsPaymentSaleCompleted() {
        Event event = new Event();
        event.setEventType(PAYMENT_SALE_COMPLETED.getPaypalEventTypeName());
        PaymentSaleCompletedProcessor processor
                = new PaymentSaleCompletedProcessor(gson);
        assertThat(processor.canProcess(event)).isTrue();
    }

    @Test
    public void whenTxnIsPending_shouldReturnChangeStateAction() {
        Transaction transaction = mock(Transaction.class);
        when(transaction.getType()).thenReturn(TransactionType.CHARGE);
        when(transaction.getState()).thenReturn(TransactionState.PENDING);
        when(transaction.getId()).thenReturn("testId");

        Payment ctpPayment = mock(Payment.class);
        when(ctpPayment.getTransactions())
                .thenReturn(Collections.singletonList(transaction));

        PaymentSaleCompletedProcessor processor
                = new PaymentSaleCompletedProcessor(gson);

        assertThat(processor.createUpdateCtpTransactionActions(ctpPayment, mock(Event.class))).isNotEmpty();
    }

    @Test
    public void whenTxnIsCharge_shouldReturnNullChangeStateAction() {
        Transaction transaction = mock(Transaction.class);
        when(transaction.getType()).thenReturn(TransactionType.CHARGE);
        when(transaction.getState()).thenReturn(TransactionState.SUCCESS);
        when(transaction.getId()).thenReturn("testId");

        Payment ctpPayment = mock(Payment.class);
        when(ctpPayment.getTransactions())
                .thenReturn(Collections.singletonList(transaction));

        PaymentSaleCompletedProcessor processor
                = new PaymentSaleCompletedProcessor(gson);

        assertThat(processor.createUpdateCtpTransactionActions(ctpPayment, mock(Event.class))).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallUpdatePayment() {
        // set up
        Payment ctpPayment = mock(Payment.class);
        NotificationProcessorBase processorBase = spy(new PaymentSaleCompletedProcessor(gson));
        
        doReturn(CompletableFuture.completedFuture(Optional.of(ctpPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
        SphereClient sphereClient = mock(SphereClient.class);

        PaymentService paymentService = spy(new PaymentServiceImpl(sphereClient));
        doReturn(CompletableFuture.completedFuture(ctpPayment))
                .when(paymentService).updatePayment(any(Payment.class), anyList());


        CtpFacade ctpFacade = spy(
                new CtpFacade(mock(CartService.class), mock(OrderService.class), paymentService)
        );

        Event event = new Event();
        event.setEventType(NotificationEventType.PAYMENT_SALE_COMPLETED.toString());

        // test
        Payment returnedPayment = processorBase.processEventNotification(ctpFacade, event)
                .toCompletableFuture()
                .join();

        // assert
        assertThat(returnedPayment).isEqualTo(ctpPayment);
        verify(paymentService, times(1))
                .updatePayment(any(Payment.class), anyList());
    }
}