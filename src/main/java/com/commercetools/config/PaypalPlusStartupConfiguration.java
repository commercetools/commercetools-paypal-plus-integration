package com.commercetools.config;

import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.validation.NotificationValidationFilter;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.notification.webhook.WebhookContainer;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Nonnull;
import javax.servlet.Filter;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;

@Configuration

// don't run PaypalPlusStartupConfiguration configuration (which will start web hooks registration)
// unless we are sure CTP projects configuration finished successfully
@DependsOn("ctpConfigStartupConfiguration")

@Import(ApplicationConfiguration.class)
@EnableCaching // used for cacheNames = "PaypalPlusConfigurationCache"
public class PaypalPlusStartupConfiguration extends WebMvcConfigurerAdapter {

    private final TenantConfigFactory tenantConfigFactory;

    private final WebhookContainer webhookContainer;

    private final PaypalPlusFacadeFactory paypalPlusFacadeFactory;

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

    /**
     * This Gson uses _ to separate words in JSON. It conforms to the JSON
     * naming conventions used in Paypal API request and responses
     */
    @Bean
    public Gson paypalGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .disableHtmlEscaping()
                .create();
    }
}