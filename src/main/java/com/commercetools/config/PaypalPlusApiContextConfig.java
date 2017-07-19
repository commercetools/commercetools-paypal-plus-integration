package com.commercetools.config;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PaypalPlusApiContextConfig {

    @Value("${paypalPlus.client.id}")
    private String clientId;

    @Value("${paypalPlus.client.secret}")
    private String clientSecret;

    @Value("${paypalPlus.client.mode}")
    private String clientMode;

    /**
     * <b>NOTE:</b> the context should not be reused for different requests, because {@link APIContext#getRequestId()}
     * is generated once and then reused, but every single request to Paypal service should have unique
     * <i>PayPal-Request-Id</i>. See more info on
     * <a href="https://developer.paypal.com/docs/api/auth-headers/">REST API authentication and headers</a>.
     *
     * @return <b>prototype-scoped</b> new instance of {@link APIContext}
     * @see com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl
     */
    @Bean
    @Scope("prototype")
    public APIContext paypalPlusApiContext() {
        return new APIContext(clientId, clientSecret, clientMode);
    }

}
