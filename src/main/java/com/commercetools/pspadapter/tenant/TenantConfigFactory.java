package com.commercetools.pspadapter.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TenantConfigFactory {

    private final Map<String, TenantProperties.Tenant> tenatNameToTenantMap;

    @Autowired
    public TenantConfigFactory(TenantProperties tenantProperties) {
        this.tenatNameToTenantMap = tenantProperties.getTenants().stream()
                .collect(Collectors.toMap(TenantProperties.Tenant::getName, Function.identity()));
    }

    public Optional<TenantConfig> getTenantConfig(@Nonnull String tenantName) {
        TenantProperties.Tenant tenant = this.tenatNameToTenantMap.get(tenantName);

        return Optional.ofNullable(tenant).map(t -> {
            TenantProperties.Tenant.Ctp ctp = t.getCtp();
            TenantProperties.Tenant.PaypalPlus paypalPlus = t.getPaypalPlus();
            return new TenantConfig(ctp.getProjectKey(), ctp.getClientId(), ctp.getClientSecret(),
                    paypalPlus.getId(), paypalPlus.getSecret(), paypalPlus.getMode());
        });
    }
}