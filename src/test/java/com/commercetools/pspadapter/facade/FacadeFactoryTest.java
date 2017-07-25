package com.commercetools.pspadapter.facade;

import com.commercetools.Application;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class FacadeFactoryTest {

    @Test
    public void whenTenantHasNoConfig_shouldReturnEmptyOptional() {
        TenantConfigFactory tenantConfigFactory = mock(TenantConfigFactory.class);
        when(tenantConfigFactory.getTenantConfig(anyString())).thenReturn(Optional.empty());

        CtpFacadeFactory ctpFacadeFactory = new CtpFacadeFactory(tenantConfigFactory);
        Optional<CtpFacade> ctFacade = ctpFacadeFactory.getCtpFacade("nonExistingTenant");
        assertThat(ctFacade).isEmpty();

        PaypalPlusFacadeFactory pPFacadeFactory = new PaypalPlusFacadeFactory(tenantConfigFactory);
        Optional<PaypalPlusFacade> pPtenant = pPFacadeFactory.getPaypalPlusFacade("nonExistingTenant");
        assertThat(pPtenant).isEmpty();
    }

    @Test
    public void whenTenantHasConfig_shouldReturnFacades() {
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("existingTenant.ctp.client.projectKey", "ctpClientId");
        mockEnv.setProperty("existingTenant.ctp.client.clientId", "ctpClientSecret");
        mockEnv.setProperty("existingTenant.ctp.client.clientSecret", "ctpProjectKey");
        mockEnv.setProperty("existingTenant.paypalPlus.client.clientId", "pPClientId");
        mockEnv.setProperty("existingTenant.paypalPlus.client.clientSecret", "pPClientSecret");
        mockEnv.setProperty("existingTenant.paypalPlus.client.mode", "sandbox");

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(mockEnv);

        CtpFacadeFactory ctpFacadeFactory = new CtpFacadeFactory(tenantConfigFactory);
        Optional<CtpFacade> ctTenant = ctpFacadeFactory.getCtpFacade("existingTenant");
        assertThat(ctTenant).isNotEmpty();

        PaypalPlusFacadeFactory pPFacadeFactory = new PaypalPlusFacadeFactory(tenantConfigFactory);
        Optional<PaypalPlusFacade> pPTenant = pPFacadeFactory.getPaypalPlusFacade("existingTenant");
        assertThat(pPTenant).isNotEmpty();
    }

}