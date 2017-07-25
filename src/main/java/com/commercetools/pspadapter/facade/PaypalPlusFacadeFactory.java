package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class PaypalPlusFacadeFactory {

    private final TenantConfigFactory config;

    @Autowired
    public PaypalPlusFacadeFactory(@Nonnull TenantConfigFactory config) {
        this.config = config;
    }

    public Optional<PaypalPlusFacade> getPaypalPlusFacade(@Nonnull String tenantName) {
        Optional<TenantConfig> tenantConfigOpt = config.getTenantConfig(tenantName);
        if (tenantConfigOpt.isPresent()) {
            TenantConfig tenantConfig = tenantConfigOpt.get();
            PaypalPlusPaymentService paypalPlusPaymentService = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContext());
            return Optional.of(new PaypalPlusFacade(paypalPlusPaymentService));
        } else {
            return Optional.empty();
        }
    }
}