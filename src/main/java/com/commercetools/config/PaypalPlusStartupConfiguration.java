package com.commercetools.config;

import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.validation.NotificationValidationFilter;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.notification.webhook.WebhookContainer;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Nonnull;
import javax.servlet.Filter;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;

@Configuration
@Import(ApplicationConfiguration.class)
@EnableCaching // used for cacheNames = "PaypalPlusConfigurationCache"
public class PaypalPlusStartupConfiguration extends WebMvcConfigurerAdapter {

    private final TenantConfigFactory tenantConfigFactory;

    private final WebhookContainer webhookContainer;

    private final PaypalPlusFacadeFactory paypalPlusFacadeFactory;

    @Value("${ctp.paypal.plus.integration.server.url}")
    private String integrationServerUrl;

    @Autowired
    public PaypalPlusStartupConfiguration(@Nonnull TenantConfigFactory tenantConfigFactory,
                                          @Nonnull WebhookContainer webhookContainer,
                                          @Nonnull PaypalPlusFacadeFactory paypalPlusFacadeFactory) {
        this.tenantConfigFactory = tenantConfigFactory;
        this.webhookContainer = webhookContainer;
        this.paypalPlusFacadeFactory = paypalPlusFacadeFactory;
    }

    @Bean
    public NotificationValidationInterceptor notificationValidationInterceptor() {
        return new NotificationValidationInterceptor(webhookContainer, tenantConfigFactory, paypalPlusFacadeFactory);
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