package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.commercetools.pspadapter.notification.processor.impl.DefaultNotificationProcessor;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class NotificationEventDispatcherProviderImpl implements NotificationEventDispatcherProvider {

    private final TenantConfigFactory configFactory;

    private final NotificationProcessorContainer processors;

    @Autowired
    public NotificationEventDispatcherProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                                   @Nonnull DefaultNotificationProcessor defaultNotificationProcessor,
                                                   // when you have Map here instead of ImmutableMap, Spring injects a default bean map
                                                   // which is not notificationProcessors that is defined in ApplicationConfig
                                                   @Nonnull NotificationProcessorContainer notificationProcessors) {
        this.configFactory = configFactory;
        this.processors = notificationProcessors;
    }

    @Override
    public Optional<NotificationDispatcher> getNotificationDispatcher(@Nonnull String tenantName) {
        return this.configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();
                    return new NotificationDispatcher(this.processors, ctpFacade);
                });
    }
}