package com.commercetools.pspadapter.facade;

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
public class CtpFacadeFactory {

    private final TenantConfigFactory config;

    @Autowired
    public CtpFacadeFactory(@Nonnull TenantConfigFactory config) {
        this.config = config;
    }

    public Optional<CtpFacade> getCtpFacade(@Nonnull String tenantName) {
        Optional<TenantConfig> tenantConfigOpt = this.config.getTenantConfig(tenantName);
        if (tenantConfigOpt.isPresent()) {
            TenantConfig tenantConfig = tenantConfigOpt.get();
            SphereClient sphereClient = tenantConfig.createSphereClient();
            CartServiceImpl cartService = new CartServiceImpl(sphereClient);
            OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
            PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
            return Optional.of(new CtpFacade(cartService, orderService, paymentService));
        } else {
            return Optional.empty();
        }
    }
}