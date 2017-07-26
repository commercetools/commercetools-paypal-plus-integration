package com.commercetools.pspadapter.facade;

import com.commercetools.Application;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static com.commercetools.testUtil.mockObjects.TenantPropertiesMock.setUpMockTenantProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class CtpFacadeFactoryTest {

    @Test
    public void whenTenantHasNoConfig_shouldReturnEmptyOptional() {
        TenantConfigFactory tenantConfigFactory = mock(TenantConfigFactory.class);
        when(tenantConfigFactory.getTenantConfig(anyString())).thenReturn(Optional.empty());

        CtpFacadeFactory ctpFacadeFactory = new CtpFacadeFactory(tenantConfigFactory);
        Optional<CtpFacade> ctFacade = ctpFacadeFactory.getCtpFacade("nonExistingTenant");
        assertThat(ctFacade).isEmpty();
    }

    @Test
    public void whenTenantHasConfig_shouldReturnFacades() {
        String existingTenantName = "existingTenant";
        TenantProperties tenantProperties = setUpMockTenantProperties(existingTenantName);

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);
        CtpFacadeFactory ctpFacadeFactory = new CtpFacadeFactory(tenantConfigFactory);
        Optional<CtpFacade> ctTenant = ctpFacadeFactory.getCtpFacade(existingTenantName);
        assertThat(ctTenant).isNotEmpty();
    }
}