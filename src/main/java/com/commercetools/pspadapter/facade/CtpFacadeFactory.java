package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import io.sphere.sdk.client.SphereClient;

import javax.annotation.Nonnull;

public class CtpFacadeFactory {

    public CtpFacadeFactory() {
    }

    public CtpFacade getCtpFacade(@Nonnull TenantConfig tenantConfig) {
        SphereClient sphereClient = tenantConfig.createSphereClient();
        CartServiceImpl cartService = new CartServiceImpl(sphereClient);
        OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
        PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
        return new CtpFacade(cartService, orderService, paymentService);

    }
}