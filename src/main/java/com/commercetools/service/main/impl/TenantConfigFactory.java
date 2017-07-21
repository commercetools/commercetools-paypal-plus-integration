package com.commercetools.service.main.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TenantConfigFactory {

    private Environment env;

    @Autowired
    public TenantConfigFactory(Environment env) {
        this.env = env;
    }

    public TenantConfig getTenantConfig(String tenantName) {
        String projectKey = env.getProperty(tenantName + ".ctp.client.projectKey");
        String clientId = env.getProperty(tenantName + ".ctp.client.clientId");
        String clientSecret = env.getProperty(tenantName + ".ctp.client.clientSecret");
        String ppPClientId = env.getProperty(tenantName + ".paypalPlus.client.clientId");
        String ppPClientSecret = env.getProperty(tenantName + ".paypalPlus.client.clientSecret");
        String ppPClientMode = env.getProperty(tenantName + ".paypalPlus.client.mode");

        return new TenantConfig(projectKey, clientId, clientSecret,
                ppPClientId, ppPClientSecret, ppPClientMode);
    }
}