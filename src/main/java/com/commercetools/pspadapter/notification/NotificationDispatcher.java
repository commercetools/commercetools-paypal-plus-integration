package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletionStage;

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

    public CompletionStage<Payment> dispatchEvent(Event event) {
        // todo: validate received event
        NotificationProcessor notificationProcessor = processors.get(event.getEventType());
        return notificationProcessor.processEventNotification(this.ctpFacade, event);
    }
}
