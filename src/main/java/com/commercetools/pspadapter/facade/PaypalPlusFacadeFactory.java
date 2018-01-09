package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
@EnableCaching
@CacheConfig(cacheNames = "PaypalPlusConfigurationCache")
public class PaypalPlusFacadeFactory {

    public PaypalPlusFacadeFactory() {
    }

    @Cacheable(sync = true)
    public PaypalPlusFacade getPaypalPlusFacade(@Nonnull TenantConfig tenantConfig) {
        PaypalPlusPaymentServiceImpl service = new PaypalPlusPaymentServiceImpl(tenantConfig.getAPIContextFactory());
        return new PaypalPlusFacade(service);
    }
}