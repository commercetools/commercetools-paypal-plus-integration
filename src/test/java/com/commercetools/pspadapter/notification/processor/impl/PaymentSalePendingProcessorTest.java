package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentSalePendingProcessorTest {

    @Mock
    Payment ctpMockPayment;

    @Mock
    Transaction transaction;

    @Mock
    SphereClient sphereClient;

    @Mock
    CartService cartService;

    @Mock
    OrderService orderService;

    private Event event;

    private PaymentServiceImpl paymentService;

    private CtpFacade ctpFacade;

    private PaymentSalePendingProcessor processorBase;

    @Before
    public void setUp() {
        this.event = new Event();
        this.event.setEventType(NotificationEventType.PAYMENT_SALE_PENDING.toString());
        this.paymentService = spy(new PaymentServiceImpl(sphereClient));
        this.ctpFacade = spy(new CtpFacade(cartService, orderService, paymentService));
        when(ctpMockPayment.getTransactions()).thenReturn(Collections.singletonList(transaction));

        this.processorBase = spy(new PaymentSalePendingProcessor(new GsonBuilder().create()));
        doReturn(CompletableFuture.completedFuture(Optional.of(ctpMockPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
    }

    @Test
    public void shouldCallUpdatePaymentWithCorrectArgs() {
        // set up
        when(transaction.getType()).thenReturn(TransactionType.CHARGE);
        when(transaction.getState()).thenReturn(TransactionState.SUCCESS);

        // test
        doAnswer(invocation -> {
            verifyUpdatePaymentCall(ctpMockPayment, invocation);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        Payment returnedPayment = executeBlocking(processorBase.processEventNotification(ctpFacade, event));

        // assert
        assertThat(returnedPayment).isEqualTo(ctpMockPayment);
        verify(paymentService, times(1))
                .updatePayment(any(Payment.class), anyList());
    }

    @Test
    public void whenTransactionIsNotFound_shouldOnlyAddInterfaceInteraction() {
        // set up
        when(transaction.getType()).thenReturn(TransactionType.REFUND);
        when(transaction.getState()).thenReturn(TransactionState.SUCCESS);

        // test
        doAnswer(invocation -> {
            List<UpdateAction<Payment>> updateActions = invocation.getArgumentAt(1, List.class);
            assertThat(updateActions.size()).isEqualTo(1);
            UpdateAction<Payment> actual = updateActions.get(0);
            assertThat(actual).isInstanceOf(AddInterfaceInteraction.class);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        executeBlocking(processorBase.processEventNotification(ctpFacade, event));
    }

    private void verifyUpdatePaymentCall(Payment ctpMockPayment, InvocationOnMock invocation) {
        Payment payment = invocation.getArgumentAt(0, Payment.class);
        List<UpdateAction<Payment>> updateActions = invocation.getArgumentAt(1, List.class);
        assertThat(payment).isEqualTo(ctpMockPayment);
        assertThat(updateActions.size()).isEqualTo(2);
        ChangeTransactionState changeTransactionState = (ChangeTransactionState) updateActions.get(1);
        assertThat(changeTransactionState.getState()).isEqualTo(TransactionState.PENDING);
    }

}