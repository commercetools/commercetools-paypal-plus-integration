package com.commercetools.pspadapter.facade;

import com.commercetools.Application;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class FacadeFactoryTest {

    private final String EXISTING_TENANT_NAME = "existingTenant";
    
    private final String NON_EXISTING_TENANT_NAME = "nonExistingTenant";

    @Test
    public void whenTenantHasNoConfig_shouldReturnEmptyOptional() {
        TenantConfigFactory tenantConfigFactory = mock(TenantConfigFactory.class);
        when(tenantConfigFactory.getTenantConfig(anyString())).thenReturn(Optional.empty());

        CtpFacadeFactory ctpFacadeFactory = new CtpFacadeFactory(tenantConfigFactory);
        Optional<CtpFacade> ctFacade = ctpFacadeFactory.getCtpFacade(NON_EXISTING_TENANT_NAME);
        assertThat(ctFacade).isEmpty();

        PaypalPlusFacadeFactory pPFacadeFactory = new PaypalPlusFacadeFactory(tenantConfigFactory);
        Optional<PaypalPlusFacade> pPtenant = pPFacadeFactory.getPaypalPlusFacade(NON_EXISTING_TENANT_NAME);
        assertThat(pPtenant).isEmpty();
    }

    @Test
    public void whenTenantHasConfig_shouldReturnFacades() {
        TenantProperties tenantProperties = setUpMockTenantProperties();

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        CtpFacadeFactory ctpFacadeFactory = new CtpFacadeFactory(tenantConfigFactory);
        Optional<CtpFacade> ctTenant = ctpFacadeFactory.getCtpFacade(EXISTING_TENANT_NAME);
        assertThat(ctTenant).isNotEmpty();

        PaypalPlusFacadeFactory pPFacadeFactory = new PaypalPlusFacadeFactory(tenantConfigFactory);
        Optional<PaypalPlusFacade> pPTenant = pPFacadeFactory.getPaypalPlusFacade(EXISTING_TENANT_NAME);
        assertThat(pPTenant).isNotEmpty();
    }

    private TenantProperties setUpMockTenantProperties() {
        TenantProperties.Tenant.Ctp ctp = new TenantProperties.Tenant.Ctp();
        ctp.setClientId("testClientId");
        ctp.setClientSecret("testClientSecret");
        ctp.setProjectKey("testProjectKey");

        TenantProperties.Tenant.PaypalPlus paypalPlus = new TenantProperties.Tenant.PaypalPlus();
        paypalPlus.setId("ppId");
        paypalPlus.setMode("sandbox");
        paypalPlus.setSecret("ppSecret");
        
        TenantProperties.Tenant tenant = new TenantProperties.Tenant();
        tenant.setName(EXISTING_TENANT_NAME);
        tenant.setCtp(ctp);
        tenant.setPaypalPlus(paypalPlus);
        
        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setTenants(Collections.singletonList(tenant));
        return tenantProperties;
    }

}