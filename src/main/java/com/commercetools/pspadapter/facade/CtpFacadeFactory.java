package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.commercetools.service.ctp.impl.TypeServiceImpl;
import io.sphere.sdk.client.SphereClient;

public class CtpFacadeFactory {

    private final TenantConfig tenantConfig;

    public CtpFacadeFactory(TenantConfig tenantConfig) {
        this.tenantConfig = tenantConfig;
    }

    public CtpFacade getCtpFacade() {
        SphereClient sphereClient = tenantConfig.createSphereClient();
        CartServiceImpl cartService = new CartServiceImpl(sphereClient);
        OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
        PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
        TypeServiceImpl typeService = new TypeServiceImpl(sphereClient);

        return new CtpFacade(cartService, orderService, paymentService, typeService);
    }
}