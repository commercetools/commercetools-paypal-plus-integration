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
import java.util.Optional;

@Component
public class CtpExecutorFactory {

    private final TenantConfigFactory config;

    @Autowired
    public CtpExecutorFactory(@Nonnull TenantConfigFactory config) {
        this.config = config;
    }

    public Optional<CtpExecutor> getCtpExecutor(@Nonnull String tenantName) {
        Optional<TenantConfig> tenantConfigOpt = this.config.getTenantConfig(tenantName);
        if (tenantConfigOpt.isPresent()) {
            TenantConfig tenantConfig = tenantConfigOpt.get();
            SphereClient sphereClient = tenantConfig.createSphereClient();
            CartServiceImpl cartService = new CartServiceImpl(sphereClient);
            OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
            PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
            return Optional.of(new CtpExecutor(cartService, orderService, paymentService));
        } else {
            return Optional.empty();
        }
    }
}