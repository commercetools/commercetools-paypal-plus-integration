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
        TenantProperties.Tenant tenant = tenantProperties.getTenants().get(tenantName);

        return Optional.ofNullable(tenant).map(t -> {
            TenantProperties.Tenant.Ctp ctp = t.getCtp();
            TenantProperties.Tenant.PaypalPlus paypalPlus = t.getPaypalPlus();
            return new TenantConfig(ctp.getProjectKey(), ctp.getClientId(), ctp.getClientSecret(),
                    paypalPlus.getId(), paypalPlus.getSecret(), paypalPlus.getMode());
        });
    }
}