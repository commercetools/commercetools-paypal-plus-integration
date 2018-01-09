package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.testUtil.mockObjects.TenantConfigMockUtil;
import io.sphere.sdk.client.SphereClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.pspadapter.facade.CacheableConfigTestUtil.assertFactoryMethodCaching;
import static com.commercetools.testUtil.mockObjects.TenantConfigMockUtil.getMockSphereClientConfig;
import static com.commercetools.testUtil.mockObjects.TenantConfigMockUtil.getMockTenantConfig;
import static com.paypal.base.Constants.SANDBOX;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheableSphereClientFactoryImplIntegrationTest {

    @Autowired
    private SphereClientFactory cacheableSphereClientFactory;

    @Test
    public void createSphereClient_fromTenantConfig_returnsCachedValue() throws Exception {
        assertFactoryMethodCaching(cacheableSphereClientFactory,
                TenantConfigMockUtil::getMockTenantConfig, () -> getMockTenantConfig("someOtherTenantName", "someOtherProjectKey"),
                cacheableSphereClientFactory::createSphereClient);
    }

    @Test
    public void createSphereClient_fromSphereClientConfig_returnsCachedValue() throws Exception {
        assertFactoryMethodCaching(cacheableSphereClientFactory,
                TenantConfigMockUtil::getMockSphereClientConfig, () -> getMockSphereClientConfig("someOtherProjectKey"),
                cacheableSphereClientFactory::createSphereClient);
    }

    @Test
    public void createSphereClient_whenDifferentTenantsHaveSameCtpConfig_returnsSameInstance() throws Exception {
        assertThat(cacheableSphereClientFactory).isNotNull();

        // different instances of the same config
        TenantConfig config1 = new TenantConfig("tenant_1", "ctpProjectKey1", "ctpClientId1", "ctpClientSecret1",
                "XXXXXXX", "XXXXXXX", SANDBOX);
        TenantConfig configSameCtpDifferentPp = new TenantConfig("tenant_2", "ctpProjectKey1", "ctpClientId1", "ctpClientSecret1",
                "YYYYYYY", "YYYYYYY", SANDBOX);

        assertThat(config1).isNotEqualTo(configSameCtpDifferentPp);
        assertThat(config1.getSphereClientConfig()).isEqualTo(config1.getSphereClientConfig());

        SphereClient client1 = cacheableSphereClientFactory.createSphereClient(config1.getSphereClientConfig());
        SphereClient client2 = cacheableSphereClientFactory.createSphereClient(configSameCtpDifferentPp.getSphereClientConfig());
        assertThat(client1).isNotNull();
        assertThat(client1).isSameAs(client2);

        client1 = cacheableSphereClientFactory.createSphereClient(config1);
        client2 = cacheableSphereClientFactory.createSphereClient(configSameCtpDifferentPp);
        assertThat(client1).isNotNull();
        assertThat(client1).isSameAs(client2);
    }

}