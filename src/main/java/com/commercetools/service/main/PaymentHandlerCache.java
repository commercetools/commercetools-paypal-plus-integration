package com.commercetools.service.main;

import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentHandlerCache {

    private final ConfigurableEnvironment env;
    private final APIContext apiContext;
    
    private Map<String, PaymentHandler> paymentClientCache = new ConcurrentHashMap<>();

    @Autowired
    public PaymentHandlerCache(ConfigurableEnvironment env, APIContext apiContext) {
        this.env = env;
        this.apiContext = apiContext;
    }

    public PaymentHandler getPaymentHandler(String tenantName) {
        PaymentHandler paymentHandler = paymentClientCache.get(tenantName);
        if (paymentHandler == null) {
            String projectKey = env.getProperty(tenantName + ".ctp.client.projectKey");
            String clientId = env.getProperty(tenantName + ".ctp.client.clientId");
            String clientSecret = env.getProperty(tenantName + ".ctp.client.clientSecret");

            SphereClient client = ClientConfigurationUtils.createClient(SphereClientConfig.of(projectKey, clientId, clientSecret));
            paymentHandler = new PaymentHandler(client, this.apiContext);
            paymentClientCache.put(tenantName, paymentHandler);
        }
        return paymentHandler;
    }

}