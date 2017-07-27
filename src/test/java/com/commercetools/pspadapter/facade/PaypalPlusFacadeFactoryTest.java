package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.Optional;

import static com.commercetools.testUtil.mockObjects.TenantPropertiesMock.setUpMockTenantProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(BlockJUnit4ClassRunner.class)
public class PaypalPlusFacadeFactoryTest {

    @Test
    public void whenTenantHasNoConfig_shouldReturnEmptyOptional() {
        TenantConfigFactory tenantConfigFactory = mock(TenantConfigFactory.class);
        when(tenantConfigFactory.getTenantConfig(anyString())).thenReturn(Optional.empty());

        PaypalPlusFacadeFactory pPFacadeFactory = new PaypalPlusFacadeFactory(tenantConfigFactory);
        Optional<PaypalPlusFacade> pPtenant = pPFacadeFactory.getPaypalPlusFacade("nonExistingTenant");
        assertThat(pPtenant).isEmpty();
    }

    @Test
    public void whenTenantHasConfig_shouldReturnFacades() {
        String existingTenantName = "existingTenant";

        TenantProperties tenantProperties = setUpMockTenantProperties(existingTenantName);

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        
        PaypalPlusFacadeFactory pPFacadeFactory = new PaypalPlusFacadeFactory(tenantConfigFactory);
        Optional<PaypalPlusFacade> pPTenant = pPFacadeFactory.getPaypalPlusFacade(existingTenantName);
        assertThat(pPTenant).isNotEmpty();
    }
}