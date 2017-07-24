package com.commercetools.testUtil.customTestConfigs;

import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CtpClientConfig {

    @Value("${paypalplus-integration-test.ctp.client.projectKey}")
    private String projectKey;

    @Value("${paypalplus-integration-test.ctp.client.clientId}")
    private String clientId;

    @Value("${paypalplus-integration-test.ctp.client.clientSecret}")
    private String clientSecret;

    @Bean
    public SphereClient sphereClient() {
        SphereClientConfig sphereClientConfig = SphereClientConfig.of(projectKey, clientId, clientSecret);
        return SphereClientFactory.of().createClient(sphereClientConfig);
    }
}
