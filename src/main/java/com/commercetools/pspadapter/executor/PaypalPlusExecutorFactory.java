package com.commercetools.pspadapter.executor;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PaypalPlusExecutorFactory {

    private final TenantConfigFactory config;

    @Autowired
    public PaypalPlusExecutorFactory(@Nonnull TenantConfigFactory config) {
        this.config = config;
    }

    public PaypalPlusExecutor getPayPalPlusExecutor(@Nonnull String tenantName) {
        TenantConfig tenantConfig = config.getTenantConfig(tenantName);
        PaypalPlusPaymentService paypalPlusPaymentService = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContext());
        return new PaypalPlusExecutor(paypalPlusPaymentService);
    }
}