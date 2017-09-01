package com.commercetools.config;

import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.validation.NotificationValidationFilter;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.paypal.api.payments.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;
import static io.sphere.sdk.utils.CompletableFutureUtils.listOfFuturesToFutureOfList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Configuration
public class PaypalPlusStartupConfiguration extends WebMvcConfigurerAdapter {

    private final TenantConfigFactory tenantConfigFactory;

    private final List<TenantConfig> tenantConfigs;

    @Value("${ctp.paypal.plus.integration.server.url}")
    private String integrationServerUrl;

    @Autowired
    public PaypalPlusStartupConfiguration(TenantConfigFactory tenantConfigFactory, List<TenantConfig> tenantConfigs) {
        this.tenantConfigFactory = tenantConfigFactory;
        this.tenantConfigs = tenantConfigs;
    }

    @Bean
    public CompletableFuture<Map<String, Webhook>> tenantToWebhookMapFuture() {
        // create all necessary webhooks. This will certainly takes a while because it involves Paypal calls,
        // but without the webhooks configured correctly, the app cannot work properly

        List<CompletionStage<WebhookWithTenantName>> webhookStages = tenantConfigs.stream()
                .map(tenantConfig -> {
                    PaypalPlusFacade paypalPlusFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();
                    return paypalPlusFacade.getPaymentService()
                            .ensureWebhook(format("%s/%s/%s", integrationServerUrl, tenantConfig.getCtpProjectKey(), NOTIFICATION_PATH_URL))
                            .thenApply(webhook -> new WebhookWithTenantName(webhook, tenantConfig.getTenantName()));
                })
                .collect(toList());

        return listOfFuturesToFutureOfList(webhookStages)
                .thenApply(webhooks -> webhooks.stream()
                        .collect(toMap(WebhookWithTenantName::getTenantName, WebhookWithTenantName::getWebhook)));
    }

    @Bean
    public NotificationValidationInterceptor notificationValidationInterceptor() {
        return new NotificationValidationInterceptor(tenantToWebhookMapFuture(), tenantConfigFactory);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // for devs: notificationValidationInterceptor() call is intercepted by Spring,
        // so it does not directly call the method above, but Spring returns
        // interceptor with all correct autowired dependencies
        registry.addInterceptor(notificationValidationInterceptor())
                .addPathPatterns("/*/" + NOTIFICATION_PATH_URL);
        super.addInterceptors(registry);
    }

    @Bean
    public Filter notificationValidationFilter() {
        return new NotificationValidationFilter();
    }
}

class WebhookWithTenantName {
    private Webhook webhook;
    private String tenantName;

    public WebhookWithTenantName(@Nonnull Webhook webhook,
                                 @Nonnull String tenantName) {
        this.webhook = webhook;
        this.tenantName = tenantName;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public String getTenantName() {
        return tenantName;
    }
}