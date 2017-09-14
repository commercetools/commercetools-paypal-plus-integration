package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
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
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@SuppressWarnings("unchecked")
public class PaymentSaleReversedProcessorTest extends BaseNotificationTest {

    private static final String CREATE_TIME_VALUE = "2014-10-31T15:41:51Z";

    private static final String REVERSED_AMOUNT = "-0.49";

    private static final String REVERSED_CURRENCY = "USD";

    @Test
    public void shouldCallUpdatePaymentWithCorrectArgs() {
        // set up
        String testInteractionId = "testInteractionId";

        Payment ctpMockPayment = createMockPayment(testInteractionId, TransactionType.CHARGE, TransactionState.SUCCESS);

        NotificationProcessorBase processorBase = spy(new PaymentSaleReversedProcessor(new GsonBuilder().create()));

        doReturn(CompletableFuture.completedFuture(Optional.of(ctpMockPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
        SphereClient sphereClient = mock(SphereClient.class);

        PaymentService paymentService = spy(new PaymentServiceImpl(sphereClient));

        CtpFacade ctpFacade = spy(
                new CtpFacade(mock(CartService.class), mock(OrderService.class), paymentService)
        );

        Map<String, String> amountMap = ImmutableMap.of(TOTAL, REVERSED_AMOUNT, CURRENCY, REVERSED_CURRENCY);
        Map<String, Object> resourceMap = ImmutableMap.of(ID, testInteractionId, AMOUNT, amountMap, CREATE_TIME, CREATE_TIME_VALUE);

        Event event = new Event();
        event.setEventType(NotificationEventType.PAYMENT_SALE_REVERSED.toString());
        event.setResource(resourceMap);

        // test
        doAnswer(invocation -> {
            verifyUpdatePaymentCall(ctpMockPayment, invocation, TransactionState.FAILURE);
            return CompletableFuture.completedFuture(ctpMockPayment);
        }).when(paymentService).updatePayment(any(Payment.class), anyList());

        Payment returnedPayment = executeBlocking(processorBase.processEventNotification(ctpFacade, event));

        // assert
        assertThat(returnedPayment).isEqualTo(ctpMockPayment);
        verify(paymentService, times(1))
                .updatePayment(any(Payment.class), anyList());
    }



}