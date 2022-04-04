package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.NotificationDispatcher;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNotificationProcessorTest {

    @Mock
    private CtpFacade ctpFacade;

    @Mock
    private Payment payment;

    private PaypalPlusFormatter paypalPlusFormatter = new PaypalPlusFormatterImpl();

    @Test
    public void whenNotificationHasNoProcessor_defaultProcessorIsUsed() {
        Gson gson = new GsonBuilder().create();
        DefaultNotificationProcessor mockDefaultProcessor = mock(DefaultNotificationProcessor.class);
        when(mockDefaultProcessor.processEventNotification(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(payment));
        PaymentSaleCompletedProcessor paymentCompletedProcessor = new PaymentSaleCompletedProcessor(gson, paypalPlusFormatter);
        PaymentSaleRefundedProcessor paymentRefundedProcessor = new PaymentSaleRefundedProcessor(gson, paypalPlusFormatter);
        PaymentSaleDeniedProcessor paymentDeniedProcessor = new PaymentSaleDeniedProcessor(gson, paypalPlusFormatter);
        PaymentSaleReversedProcessor paymentReversedProcessor = new PaymentSaleReversedProcessor(gson, paypalPlusFormatter);

        List<PaymentSaleNotificationProcessorBase> processors = Arrays.asList(paymentCompletedProcessor, paymentRefundedProcessor, paymentDeniedProcessor, paymentReversedProcessor);

        NotificationProcessorContainer container = new NotificationProcessorContainerImpl(processors, mockDefaultProcessor);

        Event event = new Event();
        event.setEventType("testEventType");

        NotificationDispatcher dispatcher = new NotificationDispatcher(container, ctpFacade);
        PaymentHandleResponse response = executeBlocking(dispatcher.handleEvent(event, "testTenant"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }
}
