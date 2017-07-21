package com.commercetools.service.main.impl;

import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: add class description
 */
@Component
public class PaypalPlusExecutorFactory {

    private final TenantConfigFactory config;

    @Autowired
    public PaypalPlusExecutorFactory(TenantConfigFactory config) {
        this.config = config;
    }

    public PaypalPlusExecutor getPayPalPlusExecutor(String tenantName) {
        TenantConfig tenantConfig = config.getTenantConfig(tenantName);
        PaypalPlusPaymentService paypalPlusPaymentService = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContext());
        return new PaypalPlusExecutor(paypalPlusPaymentService);
    }
}