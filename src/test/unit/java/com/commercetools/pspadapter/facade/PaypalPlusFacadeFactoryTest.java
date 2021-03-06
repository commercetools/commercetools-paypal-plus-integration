package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static com.commercetools.testUtil.mockObjects.TenantPropertiesMock.setUpMockTenantProperties;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class PaypalPlusFacadeFactoryTest {

    @Test
    public void whenTenantHasConfig_shouldReturnFacades() {
        String existingTenantName = "existingTenant";
        TenantProperties tenantProperties = setUpMockTenantProperties(existingTenantName);

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        PaypalPlusFacade pPTenant = new PaypalPlusFacadeFactory().getPaypalPlusFacade(tenantConfigFactory.getTenantConfig(existingTenantName).get());
        assertThat(pPTenant).isNotNull();
    }
}