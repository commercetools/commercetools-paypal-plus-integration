package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * Mock interceptor which just let all the notification requests through
 * without validation
 */
public class MockNotificationValidationInterceptor extends NotificationValidationInterceptor {

    public MockNotificationValidationInterceptor(TenantConfigFactory tenantConfigFactory) {
        super(supplyAsync(ImmutableMap::of), tenantConfigFactory);
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) throws Exception {
        return true;
    }
}