package com.commercetools.pspadapter.notification.processor;

import javax.annotation.Nonnull;

public interface NotificationProcessorContainer {

    /**
     * For a Paypal Plus event, returns a corresponding processor.
     * If none is found, return {@link com.commercetools.pspadapter.notification.processor.impl.DefaultNotificationProcessor}
     */
    NotificationProcessor getNotificationProcessor(@Nonnull String paypalEventTypeName);
}