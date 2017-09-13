package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.*;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentSalePendingProcessorTest {

    private static final String TEST_INTERACTION_ID = "testInteractionId";
    @Mock
    private Payment ctpMockPayment;

    @Mock
    private Transaction existingCtpTransaction;

    @Mock
    private SphereClient sphereClient;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    private Event event;

    private PaymentServiceImpl paymentService;

    private CtpFacade ctpFacade;

    private PaymentSalePendingProcessor processorBase;

    @Before
    public void setUp() {
        Map<String, Object> resourceMap = ImmutableMap.of(
                ID, TEST_INTERACTION_ID,
                AMOUNT, ImmutableMap.of(TOTAL, "1", CURRENCY, "USD"),
                CREATE_TIME, "2014-10-31T15:41:51Z"
        );
        this.event = new Event();
        this.event.setEventType(NotificationEventType.PAYMENT_SALE_PENDING.toString());
        this.event.setResource(resourceMap);

        when(existingCtpTransaction.getInteractionId()).thenReturn(TEST_INTERACTION_ID);

        this.paymentService = spy(new PaymentServiceImpl(sphereClient));
        this.ctpFacade = spy(new CtpFacade(cartService, orderService, paymentService));
        when(ctpMockPayment.getTransactions()).thenReturn(Collections.singletonList(existingCtpTransaction));

        this.processorBase = spy(new PaymentSalePendingProcessor(new GsonBuilder().create()));
        doReturn(CompletableFuture.completedFuture(Optional.of(ctpMockPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
    }

    @Test
    public void shouldCallUpdatePaymentWithCorrectArgs() {
        // set up
        when(existingCtpTransaction.getType()).thenReturn(TransactionType.CHARGE);
        when(existingCtpTransaction.getState()).thenReturn(TransactionState.SUCCESS);

        // test
        doAnswer(invocation -> {
            verifyUpdatePaymentCall(ctpMockPayment, invocation);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        executeBlocking(processorBase.processEventNotification(ctpFacade, event));
    }

    @Test
    public void whenTransactionIsNotFound_shouldAddNewTransaction() {
        // set up
        when(existingCtpTransaction.getType()).thenReturn(TransactionType.REFUND);
        when(existingCtpTransaction.getInteractionId()).thenReturn(TEST_INTERACTION_ID + "_random_text");
        when(existingCtpTransaction.getState()).thenReturn(TransactionState.SUCCESS);

        // test
        doAnswer(invocation -> {
            List<UpdateAction<Payment>> updateActions = invocation.getArgumentAt(1, List.class);
            assertThat(updateActions.size()).isEqualTo(2);
            UpdateAction<Payment> addInterfaceInteractionAction = updateActions.get(0);
            assertThat(addInterfaceInteractionAction).isInstanceOf(AddInterfaceInteraction.class);
            AddTransaction addTransactionAction = (AddTransaction) updateActions.get(1);
            assertThat(addTransactionAction.getTransaction().getType()).isEqualTo(TransactionType.CHARGE);
            assertThat(addTransactionAction.getTransaction().getState()).isEqualTo(TransactionState.PENDING);
            assertThat(addTransactionAction.getTransaction().getInteractionId()).isEqualTo(TEST_INTERACTION_ID);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        Payment returnedPayment = executeBlocking(processorBase.processEventNotification(ctpFacade, event));

        // assert
        assertThat(returnedPayment).isSameAs(ctpMockPayment);
    }

    private void verifyUpdatePaymentCall(Payment ctpMockPayment, InvocationOnMock invocation) {
        Payment payment = invocation.getArgumentAt(0, Payment.class);
        List<UpdateAction<Payment>> updateActions = invocation.getArgumentAt(1, List.class);
        assertThat(payment).isEqualTo(ctpMockPayment);
        assertThat(updateActions.size()).isEqualTo(2);
        // One of the action is AddInterfaceInteraction, which is common for all notification processors.
        // I already covered this case in {whenTransactionIsNotFound_shouldAddNewTransaction},
        // so it's not necessary to repeat it here.
        ChangeTransactionState changeTransactionState = (ChangeTransactionState) updateActions.get(1);
        assertThat(changeTransactionState.getState()).isEqualTo(TransactionState.PENDING);
    }

}