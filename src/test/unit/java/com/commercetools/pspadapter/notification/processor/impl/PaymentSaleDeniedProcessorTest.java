package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.TypeService;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.constants.paypalPlus.NotificationEventData.ID;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PaymentSaleDeniedProcessorTest extends BaseNotificationTest {

    private PaypalPlusFormatter paypalPlusFormatter = new PaypalPlusFormatterImpl();

    @Test
    public void shouldCallUpdatePaymentWithCorrectArgs() {
        // set up
        String testInteractionId = "testInteractionId";

        Payment ctpMockPayment = createMockPayment(testInteractionId, TransactionType.CHARGE, TransactionState.PENDING);

        NotificationProcessorBase processorBase = spy(new PaymentSaleDeniedProcessor(new GsonBuilder().create(), paypalPlusFormatter));

        doReturn(CompletableFuture.completedFuture(Optional.of(ctpMockPayment)))
                .when(processorBase).getRelatedCtpPayment(any(), any());
        SphereClient sphereClient = mock(SphereClient.class);

        PaymentService paymentService = spy(new PaymentServiceImpl(sphereClient));

        CtpFacade ctpFacade = spy(
                new CtpFacade(mock(CartService.class), mock(OrderService.class), paymentService, mock(TypeService.class))
        );

        Map<String, String> resourceMap = ImmutableMap.of(ID, testInteractionId);

        Event event = new Event();
        event.setEventType(NotificationEventType.PAYMENT_SALE_DENIED.toString());
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