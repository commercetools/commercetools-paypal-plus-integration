package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

public class NotificationDispatcher {

    private final NotificationProcessorContainer processors;

    private final CtpFacade ctpFacade;

    public NotificationDispatcher(@Nonnull NotificationProcessorContainer processorContainer,
                                  @Nonnull CtpFacade ctpFacade) {
        this.processors = processorContainer;
        this.ctpFacade = ctpFacade;
    }

    public CompletionStage<Payment> dispatchEvent(@Nonnull Event event) {
        NotificationProcessor notificationProcessor = processors.getNotificationProcessor(event.getEventType());
        return notificationProcessor.processEventNotification(this.ctpFacade, event);
    }
}
