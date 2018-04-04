package com.commercetools.pspadapter.facade;

import com.commercetools.testUtil.mockObjects.TenantConfigMockUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.pspadapter.facade.CacheableConfigTestUtil.assertFactoryMethodCaching;
import static com.commercetools.testUtil.mockObjects.TenantConfigMockUtil.getMockTenantConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CtpFacadeFactoryIT {

    @Autowired
    private CtpFacadeFactory ctpFacadeFactory;

    @Test
    public void getCtpFacade_returnsCachedValue() throws Exception {
        assertFactoryMethodCaching(ctpFacadeFactory,
                TenantConfigMockUtil::getMockTenantConfig, () -> getMockTenantConfig("someOtherTenantName"),
                ctpFacadeFactory::getCtpFacade);
    }
}