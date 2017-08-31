package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.notification.processor.impl.DefaultNotificationProcessor;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class NotificationDispatcher {

    private final Map<String, NotificationProcessor> processors;

    private final CtpFacade ctpFacade;
    
    private NotificationProcessor defaultNotificationProcessor;

    public NotificationDispatcher(@Nonnull Map<String, NotificationProcessor> processors,
                                  @Nonnull CtpFacade ctpFacade,
                                  @Nonnull DefaultNotificationProcessor defaultNotificationProcessor) {
        this.processors = processors;
        this.ctpFacade = ctpFacade;
        this.defaultNotificationProcessor = defaultNotificationProcessor;
    }

    public CompletionStage<Payment> dispatchEvent(@Nonnull Event event) {
        NotificationProcessor notificationProcessor = processors.getOrDefault(event.getEventType(), this.defaultNotificationProcessor);
        return notificationProcessor.processEventNotification(this.ctpFacade, event);
    }
}
