package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.paypal.api.payments.Event;

import javax.annotation.Nonnull;
import java.util.Map;

public class NotificationDispatcher {

    private final Map<String, NotificationProcessor> processors;

    private final CtpFacade ctpFacade;
    
    private final PaypalPlusFacade paypalPlusFacade;

    public NotificationDispatcher(@Nonnull Map<String, NotificationProcessor> processors,
                                  @Nonnull CtpFacade ctpFacade,
                                  @Nonnull PaypalPlusFacade paypalPlusFacade) {
        this.processors = processors;
        this.ctpFacade = ctpFacade;
        this.paypalPlusFacade = paypalPlusFacade;
    }

    public void dispatchEvent(Event event) {
        // todo: validate received event
        NotificationProcessor notificationProcessor = processors.get(event.getEventType());
        notificationProcessor.processEventNotification(this.ctpFacade, event).toCompletableFuture().join();
    }
}
