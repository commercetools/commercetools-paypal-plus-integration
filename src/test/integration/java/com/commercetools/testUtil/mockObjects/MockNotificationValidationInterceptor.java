package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.test.pspadapter.notification.webhook.EmptyWebhookContainerTestImpl;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock interceptor which just let all the notification requests through
 * without validation
 */
public class MockNotificationValidationInterceptor extends NotificationValidationInterceptor {

    public MockNotificationValidationInterceptor(TenantConfigFactory tenantConfigFactory) {
        super(new EmptyWebhookContainerTestImpl(), tenantConfigFactory, new PaypalPlusFacadeFactory());
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        return true;
    }
}