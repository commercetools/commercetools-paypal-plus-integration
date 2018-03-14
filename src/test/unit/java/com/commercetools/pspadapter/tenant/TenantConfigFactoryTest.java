package com.commercetools.pspadapter.tenant;

import com.commercetools.testUtil.mockObjects.TenantPropertiesMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class TenantConfigFactoryTest {

    private static final String EXISTING_TENANT_NAME = "existingTenant";

    @Test
    public void whenTenantNameDoesNotExist_shouldReturnEmptyConfig() {
        TenantProperties tenantProperties = TenantPropertiesMock.setUpMockTenantProperties(EXISTING_TENANT_NAME);

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        Optional<TenantConfig> nonExistingTenant = tenantConfigFactory.getTenantConfig("nonExistingTenant");
        assertThat(nonExistingTenant).isEmpty();
    }

    @Test
    public void whenTenantNameExists_shouldReturnConfig() {
        TenantProperties tenantProperties = TenantPropertiesMock.setUpMockTenantProperties(EXISTING_TENANT_NAME, "Tenant 2", "Tenant 3");

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        Optional<TenantConfig> nonExistingTenant = tenantConfigFactory.getTenantConfig(EXISTING_TENANT_NAME);
        assertThat(nonExistingTenant).isNotEmpty();
        assertThat(nonExistingTenant.get().getCtpProjectKey()).isEqualToIgnoringCase(EXISTING_TENANT_NAME);
    }

    @Test
    public void getTenantConfigs() throws Exception {
        TenantProperties tenantProperties = TenantPropertiesMock.setUpMockTenantProperties(EXISTING_TENANT_NAME, "Tenant 42", "Tenant 69");

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        List<TenantConfig> tenantConfigs = tenantConfigFactory.getTenantConfigs();

        assertThat(tenantConfigs.stream().map(TenantConfig::getTenantName))
                .containsExactlyInAnyOrder(EXISTING_TENANT_NAME, "Tenant 42", "Tenant 69");
    }
}