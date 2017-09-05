package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.stereotype.Component;

@Component
public class PaypalPlusFacadeFactory {

    public PaypalPlusFacadeFactory() {
    }

    public PaypalPlusFacade getPaypalPlusFacade(TenantConfig tenantConfig) {
        PaypalPlusPaymentServiceImpl service = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContextFactory());
        return new PaypalPlusFacade(service);
    }
}