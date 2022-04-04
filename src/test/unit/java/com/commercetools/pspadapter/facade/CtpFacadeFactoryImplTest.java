package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.pspadapter.tenant.TenantProperties;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.commercetools.testUtil.mockObjects.TenantPropertiesMock.setUpMockTenantProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CtpFacadeFactoryImplTest {

    @Mock(lenient = true)
    private SphereClientFactory sphereClientFactory;

    @Mock(lenient = true)
    private SphereClient sphereClient;

    @Before
    public void setUp() throws Exception {
        when(sphereClientFactory.createSphereClient(any(TenantConfig.class))).thenReturn(sphereClient);
        when(sphereClientFactory.createSphereClient(any(SphereClientConfig.class))).thenReturn(sphereClient);
    }

    @Test
    public void whenTenantHasConfig_shouldReturnFacades() {
        String existingTenantName = "existingTenant";
        TenantProperties tenantProperties = setUpMockTenantProperties(existingTenantName);

        TenantConfigFactory tenantConfigFactory = new TenantConfigFactory(tenantProperties);

        CtpFacade ctTenant = new CtpFacadeFactoryImpl(sphereClientFactory)
                .getCtpFacade(tenantConfigFactory.getTenantConfig(existingTenantName).orElseThrow(IllegalStateException::new));

        assertThat(ctTenant).isNotNull();
        assertThat(ctTenant.getCartService()).isNotNull();
        assertThat(ctTenant.getOrderService()).isNotNull();
        assertThat(ctTenant.getPaymentService()).isNotNull();
    }
}
