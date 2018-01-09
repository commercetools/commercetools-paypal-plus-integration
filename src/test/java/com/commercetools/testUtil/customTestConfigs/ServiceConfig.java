package com.commercetools.testUtil.customTestConfigs;

import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import com.commercetools.testUtil.TestConstants;
import com.commercetools.testUtil.mockObjects.MockNotificationValidationInterceptor;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.commercetools.pspadapter.util.CtpClientConfigurationUtils.createSphereClient;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;

@Configuration
public class ServiceConfig {

    @Autowired
    TenantProperties tenantProperties;

    @Bean
    @Autowired
    public CartService cartService(SphereClient sphereClient) {
        return new CartServiceImpl(sphereClient);
    }

    @Bean
    @Autowired
    public OrderService orderService(SphereClient sphereClient) {
        return new OrderServiceImpl(sphereClient);
    }

    @Bean
    @Autowired
    public PaymentService paymentService(SphereClient sphereClient) {
        return new PaymentServiceImpl(sphereClient);
    }

    @Bean
    @Autowired
    public PaypalPlusPaymentService paypalPlusPaymentService(APIContextFactory apiContextFactory) {
        return new PaypalPlusPaymentServiceImpl(apiContextFactory);
    }

    /**
     * @return {@link SphereClient} for default {@link TestConstants#MAIN_TEST_TENANT_NAME} config
     */
    @Bean
    public SphereClient sphereClient() {
        TenantProperties.Tenant.Ctp ctp = tenantProperties.getTenants().get(MAIN_TEST_TENANT_NAME).getCtp();
        SphereClientConfig sphereClientConfig = SphereClientConfig.of(ctp.getProjectKey(), ctp.getClientId(), ctp.getClientSecret());

        return createSphereClient(sphereClientConfig);
    }

    @Bean
    public APIContextFactory apiContextFactory() {
        TenantProperties.Tenant.PaypalPlus paypalPlus = tenantProperties.getTenants().get(MAIN_TEST_TENANT_NAME).getPaypalPlus();
        return new APIContextFactory(paypalPlus.getId(), paypalPlus.getSecret(), paypalPlus.getMode());
    }

    @Bean
    @Autowired
    public NotificationValidationInterceptor notificationValidationInterceptor(TenantConfigFactory tenantConfigFactory) {
        return new MockNotificationValidationInterceptor(tenantConfigFactory);
    }

}