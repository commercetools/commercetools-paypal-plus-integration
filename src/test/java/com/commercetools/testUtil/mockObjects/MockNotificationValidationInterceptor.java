package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock interceptor which just let all the notification requests through
 * without validation
 */
public class MockNotificationValidationInterceptor extends NotificationValidationInterceptor {
    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) throws Exception {
        return true;
    }
}