package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.notification.processor.NotificationProcessorContainer;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class NotificationEventDispatcherProviderImpl implements NotificationEventDispatcherProvider {

    private final TenantConfigFactory configFactory;

    private final NotificationProcessorContainer processors;

    private final CtpFacadeFactory ctpFacadeFactory;

    @Autowired
    public NotificationEventDispatcherProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                                   @Nonnull NotificationProcessorContainer notificationProcessors,
                                                   @Nonnull CtpFacadeFactory ctpFacadeFactory) {
        this.configFactory = configFactory;
        this.processors = notificationProcessors;
        this.ctpFacadeFactory = ctpFacadeFactory;
    }

    @Override
    public Optional<NotificationDispatcher> getNotificationDispatcher(@Nonnull String tenantName) {
        return this.configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = ctpFacadeFactory.getCtpFacade(tenantConfig);
                    return new NotificationDispatcher(this.processors, ctpFacade);
                });
    }
}