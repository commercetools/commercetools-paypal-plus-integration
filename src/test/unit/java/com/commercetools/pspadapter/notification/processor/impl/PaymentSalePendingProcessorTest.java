package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.TypeService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.*;
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.*;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static io.sphere.sdk.models.DefaultCurrencyUnits.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PaymentSalePendingProcessorTest extends BaseNotificationTest {

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

    @Mock
    private TypeService typeService;

    private PaypalPlusFormatter paypalPlusFormatter = new PaypalPlusFormatterImpl();

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
        this.ctpFacade = spy(new CtpFacade(cartService, orderService, paymentService, typeService));
        when(ctpMockPayment.getTransactions()).thenReturn(Collections.singletonList(existingCtpTransaction));

        this.processorBase = spy(new PaymentSalePendingProcessor(new GsonBuilder().create(), paypalPlusFormatter));
        doReturn(CompletableFuture.completedFuture(Optional.of(ctpMockPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
    }

    @Test
    public void shouldCallUpdatePaymentWithCorrectArgs() {
        // set up
        Mockito.lenient().when(existingCtpTransaction.getType()).thenReturn(TransactionType.CHARGE);
        when(existingCtpTransaction.getState()).thenReturn(TransactionState.SUCCESS);

        // test
        doAnswer(invocation -> {
            verifyUpdatePaymentCall(ctpMockPayment, invocation, TransactionState.PENDING);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        executeBlocking(processorBase.processEventNotification(ctpFacade, event));
    }

    @Test
    public void whenTransactionIsNotFound_shouldAddNewTransaction() {
        // set up
        Mockito.lenient().when(existingCtpTransaction.getType()).thenReturn(TransactionType.REFUND);
        Mockito.lenient().when(existingCtpTransaction.getInteractionId()).thenReturn(TEST_INTERACTION_ID + "_random_text");
        Mockito.lenient().when(existingCtpTransaction.getState()).thenReturn(TransactionState.SUCCESS);

        // test
        doAnswer(invocation -> {
            List<UpdateAction<Payment>> updateActions = invocation.getArgument(1, List.class);
            assertThat(updateActions.size()).isEqualTo(2);
            UpdateAction<Payment> addInterfaceInteractionAction = updateActions.get(0);
            assertThat(addInterfaceInteractionAction).isInstanceOf(AddInterfaceInteraction.class);
            AddTransaction addTransactionAction = (AddTransaction) updateActions.get(1);
            TransactionDraft addedTxn = addTransactionAction.getTransaction();
            assertThat(addedTxn.getType()).isEqualTo(TransactionType.CHARGE);
            assertThat(addedTxn.getState()).isEqualTo(TransactionState.PENDING);
            assertThat(addedTxn.getInteractionId()).isEqualTo(TEST_INTERACTION_ID);
            assertThat(addedTxn.getAmount()).isEqualTo(Money.of(1, USD));

            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        Payment returnedPayment = executeBlocking(processorBase.processEventNotification(ctpFacade, event));

        // assert
        assertThat(returnedPayment).isSameAs(ctpMockPayment);
    }

}
