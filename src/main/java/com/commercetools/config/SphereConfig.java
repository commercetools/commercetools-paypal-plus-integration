package com.commercetools.config;

import io.sphere.sdk.client.SphereClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SphereConfig {

    @Value("${ctp.client.projectKey}")
    private String projectKey;

    @Value("${ctp.client.clientId}")
    private String clientId;

    @Value("${ctp.client.clientSecret}")
    private String clientSecret;

    @Bean
    public SphereClientConfig sphereClientConfig() {
        return SphereClientConfig.of(projectKey, clientId, clientSecret);
    }
}
