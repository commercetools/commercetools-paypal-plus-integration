package com.commercetools.pspadapter.notification.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationValidationFilterTest {

    @Test
    public void whenUrlIsNotification_shouldReturnMultipleReadRequestWrapper()
            throws IOException, ServletException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        FilterChain mockChain = mock(FilterChain.class);
        when(mockRequest.getServletPath()).thenReturn("/notification-test-tenant-name/paypalplus/notification");

        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0, ServletRequest.class);
            assertThat(request).isInstanceOf(MultipleReadServletRequest.class);
            return null;
        }).when(mockChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        NotificationValidationFilter filter = new NotificationValidationFilter();
        filter.initFilterBean();
        filter.doFilter(mockRequest, mockResponse, mockChain);
    }
}
