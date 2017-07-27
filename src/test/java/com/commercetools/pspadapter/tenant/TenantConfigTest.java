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
        TenantConfig tenantConfig = new TenantConfig("ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "sandbox");
        SphereClient sphereClient = tenantConfig.createSphereClient();
        assertThat(sphereClient).isNotNull();
    }

    @Test
    public void shouldCreateApiContext() {
        TenantConfig tenantConfig = new TenantConfig("ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "sandbox");
        APIContext apiContext = tenantConfig.createAPIContext();
        assertThat(apiContext).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPPlusClientModeIsEmpty_shouldThrowException() {
        TenantConfig tenantConfig = new TenantConfig("ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", "");
        tenantConfig.createAPIContext();
    }
}