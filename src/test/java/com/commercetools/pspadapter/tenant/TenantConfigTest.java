package com.commercetools.pspadapter.tenant;

import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class TenantConfigTest {

    @Test
    public void shouldCreateSphereClient() {
        TenantConfig tenantConfig = new TenantConfig("testTenant1", "ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "sandbox");
        SphereClient sphereClient = tenantConfig.createSphereClient();
        assertThat(sphereClient).isNotNull();
    }

    @Test
    public void shouldCreateApiContext() {
        TenantConfig tenantConfig = new TenantConfig("testTenant2","ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "sandbox");
        APIContext apiContext = tenantConfig.createAPIContextFactory().createAPIContext();
        assertThat(apiContext).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPPlusClientModeIsEmpty_shouldThrowException() {
        TenantConfig tenantConfig = new TenantConfig("testTenant3", "ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "");
        tenantConfig.createAPIContextFactory().createAPIContext();
    }

    @Test
    public void whenConvertedToString_ShouldNotRevealSecrets() throws Exception {
        TenantConfig tenantConfig = new TenantConfig("testTenant_name", "ctpProject_key", "ctpClient_id", "ctpClient_secret",
                "pPlusClient_id", "pPlusClient_secret", "sandbox");

        String toString = tenantConfig.toString();

        assertThat(toString).contains("testTenant_name", "ctpProject_key", "ctpClient_id", "sandbox");

        assertThat(toString)
                .doesNotContain("ctpClientSecret")
                .doesNotContain("ctpClient_secret")
                .doesNotContain("pPlusClientSecret")
                .doesNotContain("pPlusClient_secret");
    }
}