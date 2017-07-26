package com.commercetools.pspadapter.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class TenantConfigFactory {

    private final TenantProperties tenantProperties;

    @Autowired
    public TenantConfigFactory(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    public Optional<TenantConfig> getTenantConfig(@Nonnull String tenantName) {
        Optional<TenantProperties.Tenant> tenantOpt = tenantProperties.getTenants().stream()
                .filter(t -> tenantName.equals(t.getName()))
                .findAny();

        return tenantOpt.map(tenant -> {
            TenantProperties.Tenant.Ctp ctp = tenant.getCtp();
            TenantProperties.Tenant.PaypalPlus paypalPlus = tenant.getPaypalPlus();
            return new TenantConfig(ctp.getProjectKey(), ctp.getProjectKey(), ctp.getClientId(),
                    paypalPlus.getId(), paypalPlus.getSecret(), paypalPlus.getMode());
        });
    }
}