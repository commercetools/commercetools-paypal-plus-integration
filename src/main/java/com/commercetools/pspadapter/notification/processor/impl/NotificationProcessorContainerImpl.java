package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationProcessorContainerImpl implements NotificationProcessorContainer {

    private final Map<String, NotificationProcessor> notificationProcessorsMap = new HashMap<>();

    private final DefaultNotificationProcessor defaultNotificationProcessor;

    @Autowired
    public NotificationProcessorContainerImpl(@Nonnull List<PaymentSaleNotificationProcessorBase> paymentSaleProcessors,
                                              @Nonnull DefaultNotificationProcessor defaultNotificationProcessor) {
        paymentSaleProcessors.forEach(paymentSaleNotificationProcessor -> {
            String paypalEventTypeName = paymentSaleNotificationProcessor.getNotificationEventType().getPaypalEventTypeName();
            this.notificationProcessorsMap.put(paypalEventTypeName, paymentSaleNotificationProcessor);
        });
        this.defaultNotificationProcessor = defaultNotificationProcessor;
    }

    @Override
    public NotificationProcessor getNotificationProcessorOrDefault(@Nonnull String paypalEventTypeName) {
        return notificationProcessorsMap.getOrDefault(paypalEventTypeName, this.defaultNotificationProcessor);
    }
}