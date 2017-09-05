package com.commercetools.pspadapter.notification;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface NotificationEventDispatcherProvider {

    Optional<NotificationDispatcher> getNotificationDispatcher(@Nonnull String tenantName);
}
