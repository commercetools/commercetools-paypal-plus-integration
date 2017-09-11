package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.ID;
import static com.commercetools.payment.constants.paypalPlus.NotificationEventType.PAYMENT_SALE_COMPLETED;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
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
    public void whenTxnIsChargePending_shouldReturnChangeStateAction() {
        String testInteractionId = "testInteractionId";

        Payment ctpPayment = mockCtpPayment(testInteractionId, TransactionState.PENDING);

        Map<String, String> resourceMap = ImmutableMap.of(ID, testInteractionId);

        Event mockEvent = mock(Event.class);
        when(mockEvent.getResource()).thenReturn(resourceMap);

        PaymentSaleCompletedProcessor processor
                = new PaymentSaleCompletedProcessor(gson);

        assertThat(processor.createUpdatePaymentActions(ctpPayment, mockEvent)).isNotEmpty();
    }

    @Test
    public void whenTxnIsChargeSuccess_shouldReturnNullChangeStateAction() {
        String testInteractionId = "testInteractionId";

        Payment ctpPayment = mockCtpPayment(testInteractionId, TransactionState.SUCCESS);

        Map<String, String> resourceMap = ImmutableMap.of(ID, testInteractionId);

        Event mockEvent = mock(Event.class);
        when(mockEvent.getResource()).thenReturn(resourceMap);

        PaymentSaleCompletedProcessor processor
                = new PaymentSaleCompletedProcessor(gson);

        assertThat(processor.createUpdatePaymentActions(ctpPayment, mockEvent)).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldCallUpdatePayment() {
        // set up
        String testInteractionId = "testInteractionId";

        Payment ctpPayment = mockCtpPayment(testInteractionId, TransactionState.PENDING);

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

        Map<String, String> resourceMap = ImmutableMap.of(ID, testInteractionId);

        Event event = new Event();
        event.setEventType(NotificationEventType.PAYMENT_SALE_COMPLETED.toString());
        event.setResource(resourceMap);

        // test
        Payment returnedPayment = executeBlocking(processorBase.processEventNotification(ctpFacade, event));

        // assert
        assertThat(returnedPayment).isEqualTo(ctpPayment);
        verify(paymentService, times(1))
                .updatePayment(any(Payment.class), anyList());
    }

    private Payment mockCtpPayment(String testInteractionId, TransactionState transactionState) {
        Transaction transaction = mock(Transaction.class);
        when(transaction.getType()).thenReturn(TransactionType.CHARGE);
        when(transaction.getState()).thenReturn(transactionState);
        when(transaction.getId()).thenReturn("testId");
        when(transaction.getInteractionId()).thenReturn(testInteractionId);

        Payment ctpPayment = mock(Payment.class);
        when(ctpPayment.getTransactions())
                .thenReturn(Collections.singletonList(transaction));
        return ctpPayment;
    }
}