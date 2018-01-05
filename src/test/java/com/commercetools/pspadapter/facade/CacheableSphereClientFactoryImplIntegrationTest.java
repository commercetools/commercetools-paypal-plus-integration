package com.commercetools.pspadapter.facade;

import com.commercetools.testUtil.mockObjects.TenantConfigMockUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.pspadapter.facade.CacheableConfigTestUtil.assertFactoryMethodCaching;
import static com.commercetools.testUtil.mockObjects.TenantConfigMockUtil.getMockSphereClientConfig;
import static com.commercetools.testUtil.mockObjects.TenantConfigMockUtil.getMockTenantConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheableSphereClientFactoryImplIntegrationTest {

    @Autowired
    private SphereClientFactory cacheableSphereClientFactory;

    @Test
    public void createSphereClient_fromTenantConfig_returnsCachedValue() throws Exception {
        assertFactoryMethodCaching(cacheableSphereClientFactory,
                TenantConfigMockUtil::getMockTenantConfig, () -> getMockTenantConfig("someOtherTenantName"),
                cacheableSphereClientFactory::createSphereClient);
    }

    @Test
    public void createSphereClient_fromSphereClientConfig_returnsCachedValue() throws Exception {
        assertFactoryMethodCaching(cacheableSphereClientFactory,
                TenantConfigMockUtil::getMockSphereClientConfig, () -> getMockSphereClientConfig("someOtherProjectKey"),
                cacheableSphereClientFactory::createSphereClient);
    }

}