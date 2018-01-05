package com.commercetools.pspadapter.tenant;

import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static com.paypal.base.Constants.SANDBOX;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class TenantConfigTest {

    @Test
    public void shouldCreateSphereClient() {
        TenantConfig tenantConfig = new TenantConfig("testTenant1", "ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", SANDBOX);
        SphereClient sphereClient = tenantConfig.createSphereClient();
        assertThat(sphereClient).isNotNull();
    }

    @Test
    public void shouldCreateApiContext() {
        TenantConfig tenantConfig = new TenantConfig("testTenant2", "ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", SANDBOX);
        APIContext apiContext = tenantConfig.getAPIContextFactory().createAPIContext();
        assertThat(apiContext).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPPlusClientModeIsEmpty_shouldThrowException() {
        TenantConfig tenantConfig = new TenantConfig("testTenant3", "ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "");
        tenantConfig.getAPIContextFactory().createAPIContext();
    }

    @Test
    public void typeHasProperEqualsAndHashMethodsImplementation() throws Exception {
        TenantConfig tenantConfig1 = new TenantConfig("testTenant4", "ctpProjectKey4", "ctpClientId4", "ctpClientSecret4",
                "pPlusClientId4", "pPlusClientSecret4", SANDBOX);
        TenantConfig tenantConfig1_sameContent = new TenantConfig("testTenant4", "ctpProjectKey4", "ctpClientId4", "ctpClientSecret4",
                "pPlusClientId4", "pPlusClientSecret4", SANDBOX);

        TenantConfig tenantConfig_differentProjectKey = new TenantConfig("testTenant5", "ctpProjectKey4", "ctpClientId4", "ctpClientSecret4",
                "pPlusClientId4", "pPlusClientSecret4", SANDBOX);
        TenantConfig tenantConfig_differentPpPlusClientId = new TenantConfig("testTenant4", "ctpProjectKey4", "ctpClientId4", "ctpClientSecret4",
                "pPlusClientId5", "pPlusClientSecret4", SANDBOX);

        assertThat(tenantConfig1)
                .isEqualTo(tenantConfig1_sameContent);

        assertThat(tenantConfig1.hashCode()).isEqualTo(tenantConfig1_sameContent.hashCode());

        assertThat(tenantConfig1).isNotEqualTo(tenantConfig_differentProjectKey);
        assertThat(tenantConfig1).isNotEqualTo(tenantConfig_differentPpPlusClientId);
    }
}