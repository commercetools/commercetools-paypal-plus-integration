package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.commercetools.service.ctp.impl.TypeServiceImpl;
import io.sphere.sdk.client.SphereClient;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@EnableCaching
@CacheConfig(cacheNames = "CtpFacadeFactoryCache")
public class CtpFacadeFactoryImpl implements CtpFacadeFactory {

    private final SphereClientFactory sphereClientFactory;

    public CtpFacadeFactoryImpl(@Nonnull SphereClientFactory sphereClientFactory) {
        this.sphereClientFactory = sphereClientFactory;
    }

    @Override
    @Cacheable(sync = true)
    public CtpFacade getCtpFacade(@Nonnull TenantConfig tenantConfig) {
        SphereClient sphereClient = sphereClientFactory.createSphereClient(tenantConfig);
        CartServiceImpl cartService = new CartServiceImpl(sphereClient);
        OrderServiceImpl orderService = new OrderServiceImpl(sphereClient);
        PaymentServiceImpl paymentService = new PaymentServiceImpl(sphereClient);
        TypeServiceImpl typeService = new TypeServiceImpl(sphereClient);
        return new CtpFacade(cartService, orderService, paymentService, typeService);
    }
}