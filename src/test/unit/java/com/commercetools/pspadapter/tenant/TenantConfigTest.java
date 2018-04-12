package com.commercetools.pspadapter.tenant;

import com.paypal.base.rest.APIContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import static com.paypal.base.Constants.SANDBOX;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class TenantConfigTest {

    @Test
    public void shouldCreateSphereClient() {
        TenantConfig tenantConfig = new TenantConfig("testTenant1", "ctpProjectKey", "ctpClientId", "ctpClientSecret",
                "pPlusClientId", "pPlusClientSecret", SANDBOX);

        assertThat(tenantConfig.getSphereClientConfig()).isNotNull();
        assertThat(tenantConfig.getSphereClientConfig().getProjectKey()).isEqualTo("ctpProjectKey");
        assertThat(tenantConfig.getSphereClientConfig().getClientId()).isEqualTo("ctpClientId");
        assertThat(tenantConfig.getSphereClientConfig().getClientSecret()).isEqualTo("ctpClientSecret");
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

    @Test
    public void whenConvertedToString_ShouldNotRevealSecrets() throws Exception {
        TenantConfig tenantConfig = new TenantConfig("testTenant_name", "ctpProject_key", "ctpClient_id", "ctpClient_REAL_SECRET_VALUE",
                "pPlusClient_id", "pPlusClient_REAL_SECRET_VALUE", "sandbox");

        String toString = tenantConfig.toString();

        assertThat(toString).contains("testTenant_name", "ctpProject_key", "ctpClient_id", "sandbox");

        assertThat(toString)
                .doesNotContain("ctpClientSecret")
                .doesNotContain("ctpClient_REAL_SECRET_VALUE")
                .doesNotContain("pPlusClientSecret")
                .doesNotContain("pPlusClient_REAL_SECRET_VALUE");
    }

    @Test
    public void toStringExclusion_containsActualPropertiesNames() throws Exception {
        // assert that TenantConfig#EXCLUDE_FIELD_NAMES used in toString() skips actual fields
        assertThat(Stream.of(TenantConfig.class.getDeclaredFields()).map(Field::getName))
                .contains(TenantConfig.EXCLUDE_FIELD_NAMES);
    }
}