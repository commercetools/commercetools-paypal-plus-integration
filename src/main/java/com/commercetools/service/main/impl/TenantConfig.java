package com.commercetools.service.main.impl;

import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;

public class TenantConfig {

    private final String projectKey;
    private final String clientId;
    private final String clientSecret;

    private final String ppPlusClientId;
    private final String ppPlusClientSecret;
    private final String ppPlusClientMode;

    public TenantConfig(String projectKey,
                        String clientId,
                        String clientSecret,
                        String ppPlusClientId,
                        String ppPlusClientSecret,
                        String ppPlusClientMode) {
        this.projectKey = projectKey;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.ppPlusClientId = ppPlusClientId;
        this.ppPlusClientSecret = ppPlusClientSecret;
        this.ppPlusClientMode = ppPlusClientMode;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPpPlusClientId() {
        return ppPlusClientId;
    }

    public String getPpPlusClientSecret() {
        return ppPlusClientSecret;
    }

    public String getPpPlusClientMode() {
        return ppPlusClientMode;
    }

    public SphereClient createSphereClient () {
        return ClientConfigurationUtils.createClient(createCtpConfig());
    }

    public SphereClientConfig createCtpConfig () {
        return SphereClientConfig.of(
                this.projectKey,
                this.clientId,
                this.clientSecret);
    }

    public APIContext createAPIContext() {
        return new APIContext(this.ppPlusClientId, this.ppPlusClientSecret, this.ppPlusClientMode);
    }
}