package com.commercetools.pspadapter.notification.validation;

import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.webhook.WebhookContainer;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.paypal.api.payments.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static com.commercetools.util.IOUtil.getBody;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Validates if the notification request from PayPal Plus is a legit one. This intercepts all
 * requests that comes to /notification endpoint, call Paypal with correct request data.
 * If Paypal confirms legitimacy, then the request continues to controller,
 * otherwise the request is ended immediately.
 */
public class NotificationValidationInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NotificationValidationInterceptor.class);

    private static final String TENANT_NAME_PATH_VARIABLE = "tenantName";

    private final WebhookContainer webhookContainer;

    private final TenantConfigFactory configFactory;

    private final PaypalPlusFacadeFactory paypalPlusFacadeFactory;

    @Autowired
    public NotificationValidationInterceptor(@Nonnull WebhookContainer webhookContainer,
                                             @Nonnull TenantConfigFactory configFactory,
                                             @Nonnull PaypalPlusFacadeFactory paypalPlusFacadeFactory) {
        this.webhookContainer = webhookContainer;
        this.configFactory = configFactory;
        this.paypalPlusFacadeFactory = paypalPlusFacadeFactory;
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        try {
            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String tenantName = (String) pathVariables.get(TENANT_NAME_PATH_VARIABLE);

            TenantConfig tenantConfig = configFactory.getTenantConfig(tenantName).orElse(null);

            if (tenantConfig == null) {
                return false; // tenant config for this name not found - skip mapping
            }

            return this.webhookContainer.getWebhookCompletionStageByTenantName(tenantName)
                    .thenCompose(validateWebhookIfExists(request, tenantConfig))
                    .toCompletableFuture().join(); // looks like here we can't avoid blocking
        } catch (Throwable t) {
            logger.error("Error when validating the notification request from Paypal Plus", t);
            return false;
        }
    }

    private Function<Webhook, CompletionStage<Boolean>> validateWebhookIfExists(@Nonnull HttpServletRequest request,
                                                                                @Nonnull TenantConfig tenantConfig) {
        return webhook -> {
            if (webhook == null) {
                logger.info("Webhook not found for tenant tenantName=[{}]", tenantConfig.getTenantName());
                return completedFuture(false);
            }

            try {
                return paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig).getPaymentService().validateNotificationEvent(webhook, getHeadersInfo(request), getBody(request));
            } catch (Throwable error) {
                logger.error("Webhook for tenantName=[{}] can't be initialized: ", tenantConfig.getTenantName(), error);
            }

            return completedFuture(false);
        };
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

}