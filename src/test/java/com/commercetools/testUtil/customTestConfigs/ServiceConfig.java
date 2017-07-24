package com.commercetools.testUtil.customTestConfigs;

import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

@Configuration
@DependsOn({"ctpClientConfig"})
public class ServiceConfig {

    @Autowired
    private Environment env;

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
    public PaypalPlusPaymentService paypalPlusPaymentService(APIContext apiContext) {
        return new PaypalPlusPaymentServiceImpl(apiContext);
    }

    @Bean
    public APIContext apiContext(){
        String ppPClientId = env.getProperty("paypalplus-integration-test.paypalPlus.client.clientId");
        String ppPClientSecret = env.getProperty("paypalplus-integration-test.paypalPlus.client.clientSecret");
        String ppPClientMode = env.getProperty("paypalplus-integration-test.paypalPlus.client.mode");
        return new APIContext(ppPClientId, ppPClientSecret, ppPClientMode);
    }
}