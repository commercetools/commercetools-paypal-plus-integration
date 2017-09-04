package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.notification.webhook.impl.WebhookContainerImpl;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

/**
 * Mock interceptor which just let all the notification requests through
 * without validation
 */
public class MockNotificationValidationInterceptor extends NotificationValidationInterceptor {

    public MockNotificationValidationInterceptor(TenantConfigFactory tenantConfigFactory) {
        super(new WebhookContainerImpl(Collections.emptyList(),"http://test.com"), tenantConfigFactory);
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) throws Exception {
        return true;
    }
}