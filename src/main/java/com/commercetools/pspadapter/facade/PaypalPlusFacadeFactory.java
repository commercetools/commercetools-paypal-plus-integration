package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheNames = "PaypalPlusConfigurationCache")
public class PaypalPlusFacadeFactory {

    public PaypalPlusFacadeFactory() {
    }

    @Cacheable(sync = true)
    public PaypalPlusFacade getPaypalPlusFacade(TenantConfig tenantConfig) {
        PaypalPlusPaymentServiceImpl service = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContextFactory());
        return new PaypalPlusFacade(service);
    }
}