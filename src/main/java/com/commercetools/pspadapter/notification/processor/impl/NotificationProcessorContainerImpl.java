package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationProcessorContainerImpl implements NotificationProcessorContainer {

    private final Map<String, NotificationProcessor> notificationProcessorsMap = new HashMap<>();

    private final DefaultNotificationProcessor defaultNotificationProcessor;

    @Autowired
    public NotificationProcessorContainerImpl(@Nonnull PaymentSaleCompletedProcessor paymentSaleCompletedProcessor,
                                              @Nonnull DefaultNotificationProcessor defaultNotificationProcessor) {
        this.notificationProcessorsMap.put(NotificationEventType.PAYMENT_SALE_COMPLETED.getPaypalEventTypeName(), paymentSaleCompletedProcessor);
        this.defaultNotificationProcessor = defaultNotificationProcessor;
    }

    @Override
    public NotificationProcessor getNotificationProcessorOrDefault(@Nonnull String paypalEventTypeName) {
        return notificationProcessorsMap.getOrDefault(paypalEventTypeName, this.defaultNotificationProcessor);
    }
}