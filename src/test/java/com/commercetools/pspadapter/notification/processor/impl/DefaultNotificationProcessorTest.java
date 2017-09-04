package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.NotificationDispatcher;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNotificationProcessorTest {

    @Autowired
    Gson gson;

    @Test
    public void whenNotificationHasNoProcessor_defaultProcessorIsUsed () {
        CtpFacade ctpFacade = mock(CtpFacade.class);
        Payment mockPayment = mock(Payment.class);
        DefaultNotificationProcessor mockDefaultProcessor = mock(DefaultNotificationProcessor.class);
        when(mockDefaultProcessor.processEventNotification(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mockPayment));
        PaymentSaleCompletedProcessor paymentCompletedProcessor = new PaymentSaleCompletedProcessor(new GsonBuilder().create());
        
        NotificationProcessorContainer container = new NotificationProcessorContainerImpl(paymentCompletedProcessor, mockDefaultProcessor);

        Event event = new Event();
        event.setEventType("testEventType");

        NotificationDispatcher dispatcher = new NotificationDispatcher(container, ctpFacade);
        CompletionStage<Payment> paymentCompletionStage = dispatcher.dispatchEvent(event);
        assertThat(paymentCompletionStage.toCompletableFuture().join()).isEqualTo(mockPayment);
    }
}