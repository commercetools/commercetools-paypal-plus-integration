package com.commercetools.pspadapter.executor;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import io.sphere.sdk.client.SphereClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class CtpExecutorFactory {

    private final TenantConfigFactory config;

    @Autowired
    public CtpExecutorFactory(@Nonnull TenantConfigFactory config) {
        this.config = config;
    }

    public CtpExecutor getCtpExecutor(@Nonnull String tenantName) {
        TenantConfig tenantConfig = this.config.getTenantConfig(tenantName);
        SphereClient sphereClient = tenantConfig.createSphereClient();
        CartServiceImpl cartService = new CartServiceImpl(sphereClient);
        OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
        PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
        return new CtpExecutor(cartService, orderService, paymentService);
    }
}