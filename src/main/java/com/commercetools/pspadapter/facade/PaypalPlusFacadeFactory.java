package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;

public class PaypalPlusFacadeFactory {

    private final TenantConfig tenantConfig;

    public PaypalPlusFacadeFactory(TenantConfig tenantConfig) {
        this.tenantConfig = tenantConfig;
    }

    public PaypalPlusFacade getPaypalPlusFacade() {
        PaypalPlusPaymentServiceImpl service = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContextFactory());
        return new PaypalPlusFacade(service);
    }
}