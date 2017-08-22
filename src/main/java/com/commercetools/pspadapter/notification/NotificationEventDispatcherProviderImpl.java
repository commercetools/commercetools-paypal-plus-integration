package com.commercetools.pspadapter.notification;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

@Component
public class NotificationEventDispatcherProviderImpl implements NotificationEventDispatcherProvider {

    private final TenantConfigFactory configFactory;

    private final Map<String, NotificationProcessor> processors;

    public NotificationEventDispatcherProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                                   @Nonnull Map<String, NotificationProcessor> notificationProcessors) {
        this.configFactory = configFactory;
        this.processors = notificationProcessors;
    }

    @Override
    public Optional<NotificationDispatcher> getNotificationDispatcher(@Nonnull String tenantName) {
        return this.configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();
                    PaypalPlusFacade paypalPlusFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();
                    return new NotificationDispatcher(this.processors, ctpFacade, paypalPlusFacade);
                });
    }
}