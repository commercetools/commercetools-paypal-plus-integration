package com.commercetools.config;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaypalPlusApiContextConfig {

    @Value("${paypalPlus.client.id}")
    private String clientId;

    @Value("${paypalPlus.client.secret}")
    private String clientSecret;

    @Value("${paypalPlus.client.mode}")
    private String clientMode;

    @Bean
    public APIContext paypalPlusApiContext() {
        return new APIContext(clientId, clientSecret, clientMode);
    }

}
