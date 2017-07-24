package com.commercetools.pspadapter.executor;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class PaypalPlusExecutorFactory {

    private final TenantConfigFactory config;

    @Autowired
    public PaypalPlusExecutorFactory(@Nonnull TenantConfigFactory config) {
        this.config = config;
    }

    public Optional<PaypalPlusExecutor> getPayPalPlusExecutor(@Nonnull String tenantName) {
        Optional<TenantConfig> tenantConfigOpt = config.getTenantConfig(tenantName);
        if (tenantConfigOpt.isPresent()) {
            TenantConfig tenantConfig = tenantConfigOpt.get();
            PaypalPlusPaymentService paypalPlusPaymentService = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContext());
            return Optional.of(new PaypalPlusExecutor(paypalPlusPaymentService));
        } else {
            return Optional.empty();
        }
    }
}