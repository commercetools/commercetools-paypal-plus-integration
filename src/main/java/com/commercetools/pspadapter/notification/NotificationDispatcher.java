package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.paypal.api.payments.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

import static java.lang.String.format;

public class NotificationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationProcessorContainer processors;

    private final CtpFacade ctpFacade;

    public NotificationDispatcher(@Nonnull NotificationProcessorContainer processorContainer,
                                  @Nonnull CtpFacade ctpFacade) {
        this.processors = processorContainer;
        this.ctpFacade = ctpFacade;
    }

    public CompletionStage<PaymentHandleResponse> handleEvent(@Nonnull Event event,
                                                              @Nonnull String tenantName) {
        NotificationProcessor notificationProcessor = processors.getNotificationProcessor(event.getEventType());
        return notificationProcessor.processEventNotification(this.ctpFacade, event)
                .handle((payment, throwable) -> {
                    if (throwable != null) {
                        logger.error(format("Unexpected exception processing event=[%s] for tenant=[%s]",
                                event.toJSON(), tenantName), throwable);
                        return PaymentHandleResponse.ofHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                    } else {
                        return PaymentHandleResponse.ofHttpStatus(HttpStatus.OK);
                    }
                });
    }
}
