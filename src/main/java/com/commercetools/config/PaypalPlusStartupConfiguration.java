package com.commercetools.config;

import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import com.paypal.api.payments.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.commercetools.payment.constants.Psp.PSP_NAME;
import static java.lang.String.format;

@Configuration
public class PaypalPlusStartupConfiguration extends WebMvcConfigurerAdapter {

    // todo: put this to config file
    private static final String NOTIFICATION_URL_TEMPLATE = "https://53fd60b6.ngrok.io/%s/paypalplus/notification";

    @Bean
    @Autowired
    public Map<String, Webhook> tenantNameToWebhookMap(TenantConfigFactory tenantConfigFactory,
                                                       TenantProperties tenantProperties) {
        // create all necessary webhooks. This will certainly takes a while because it involves Paypal calls,
        // but without the webhooks configured correctly, the app cannot work properly
        return tenantProperties.getTenants().keySet().stream()
                .map(tenantName -> {
                    Optional<TenantConfig> tenantConfigOpt = tenantConfigFactory.getTenantConfig(tenantName);
                    return tenantConfigOpt.map(tenantConfig -> {
                        PaypalPlusFacade paypalPlusFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();
                        return paypalPlusFacade.getPaymentService()
                                .ensureWebhook(format(NOTIFICATION_URL_TEMPLATE, tenantConfig.getCtpProjectKey()))
                                .thenApply(webhook -> new WebhookWithTenantName(webhook, tenantName))
                                .toCompletableFuture().join();
                    }).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(WebhookWithTenantName::getTenantName, WebhookWithTenantName::getWebhook));
    }

    @Bean
    public NotificationValidationInterceptor notificationValidationInterceptor() {
        return new NotificationValidationInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // for devs: notificationValidationInterceptor() call is intercepted by Spring,
        // so it does not directly call the method above, but Spring returns
        // interceptor with all correct autowired dependencies
        registry.addInterceptor(notificationValidationInterceptor())
                .addPathPatterns("/*/" + PSP_NAME + "/notification");
        super.addInterceptors(registry);
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