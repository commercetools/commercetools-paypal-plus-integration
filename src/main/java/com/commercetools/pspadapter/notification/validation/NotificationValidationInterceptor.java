package com.commercetools.pspadapter.notification.validation;

import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.google.common.annotations.VisibleForTesting;
import com.paypal.api.payments.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Nonnull;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Validates if the notification request from PayPal Plus is a legit one. This intercepts all
 * requests that comes to /notification endpoint, call Paypal with correct request data.
 * If Paypal confirms legitimacy, then the request continues to controller,
 * otherwise the request is ended immediately.
 */
public class NotificationValidationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationValidationInterceptor.class);

    private final Map<String, Webhook> tenantNameToWebhookMap;

    private final TenantConfigFactory configFactory;

    @Autowired
    public NotificationValidationInterceptor(@Nonnull Map<String, Webhook> tenantNameToWebhookMap,
                                             @Nonnull TenantConfigFactory configFactory) {
        this.tenantNameToWebhookMap = tenantNameToWebhookMap;
        this.configFactory = configFactory;
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) throws Exception {
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String tenantName = (String) pathVariables.get("tenantName");
        Webhook webhook = this.tenantNameToWebhookMap.get(tenantName);
        if (webhook == null) {
            LOG.error(format("Webhook not found for tenant tenantName=[%s]", tenantName));
            return false;
        }
        Optional<TenantConfig> tenantConfigOpt = configFactory.getTenantConfig(tenantName);
        return tenantConfigOpt
                .map(this::getPaypalPlusFacade)
                .map(paypalPlusFacade -> {
                    try {
                        return paypalPlusFacade.getPaymentService().validateNotificationEvent(webhook, getHeadersInfo(request), getBody(request));
                    } catch (IOException e) {
                        return CompletableFuture.completedFuture(false);
                    }
                })
                .orElseGet(() -> CompletableFuture.completedFuture(false))
                .toCompletableFuture().join();
    }

    @VisibleForTesting
    protected PaypalPlusFacade getPaypalPlusFacade(TenantConfig tenantConfig) {
        return new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();
    }

    private Map<String, String> getHeadersInfo(@Nonnull HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    private String getBody(@Nonnull HttpServletRequest request) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                return bufferedReader.lines().collect(Collectors.joining());
            }
        } else {
            return "";
        }
    }
}