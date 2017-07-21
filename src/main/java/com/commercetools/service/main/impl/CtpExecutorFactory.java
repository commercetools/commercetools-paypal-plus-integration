package com.commercetools.service.main.impl;

import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import io.sphere.sdk.client.SphereClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CtpExecutorFactory {

    private final TenantConfigFactory config;

    @Autowired
    public CtpExecutorFactory(TenantConfigFactory config) {
        this.config = config;
    }

    public CtpExecutor getCtpExecutor(String tenantName) {
        TenantConfig tenantConfig = this.config.getTenantConfig(tenantName);
        SphereClient sphereClient = tenantConfig.createSphereClient();
        CartServiceImpl cartService = new CartServiceImpl(sphereClient);
        OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
        PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
        return new CtpExecutor(cartService, orderService, paymentService);
    }
}