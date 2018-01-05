package com.commercetools.pspadapter.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Component
@EnableCaching
@CacheConfig(cacheNames = "ApplicationConfigurationCache")
public class TenantConfigFactory {

    private final TenantProperties tenantProperties;

    @Autowired
    public TenantConfigFactory(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Cacheable(sync = true)
    public Optional<TenantConfig> getTenantConfig(@Nonnull String tenantName) {
        TenantProperties.Tenant tenant = tenantProperties.getTenants().get(tenantName);

        return Optional.ofNullable(tenant).map(t -> {
            TenantProperties.Tenant.Ctp ctp = t.getCtp();
            TenantProperties.Tenant.PaypalPlus paypalPlus = t.getPaypalPlus();
            return new TenantConfig(tenantName, ctp.getProjectKey(), ctp.getClientId(), ctp.getClientSecret(),
                    paypalPlus.getId(), paypalPlus.getSecret(), paypalPlus.getMode());
        });
    }

    /**
     * @return list of all available tenants of the application (from {@link #tenantProperties}).
     */
    @Bean
    public List<TenantConfig> getTenantConfigs() {
        return tenantProperties.getTenants().entrySet().stream()
                .map(tenantEntry -> {
                    TenantProperties.Tenant value = tenantEntry.getValue();
                    TenantProperties.Tenant.Ctp ctp = value.getCtp();
                    TenantProperties.Tenant.PaypalPlus paypalPlus = value.getPaypalPlus();
                    return new TenantConfig(tenantEntry.getKey(),
                            ctp.getProjectKey(), ctp.getClientId(), ctp.getClientSecret(),
                            paypalPlus.getId(), paypalPlus.getSecret(), paypalPlus.getMode());
                })
                .collect(toList());
    }
}