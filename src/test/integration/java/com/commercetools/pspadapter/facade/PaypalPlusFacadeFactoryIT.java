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
public class PaypalPlusFacadeFactoryIT {

    @Autowired
    private PaypalPlusFacadeFactory paypalPlusFacadeFactory;

    @Test
    public void getPaypalPlusFacade_returnsCachedValue() throws Exception {
        assertFactoryMethodCaching(paypalPlusFacadeFactory,
                TenantConfigMockUtil::getMockTenantConfig, () -> getMockTenantConfig("someOtherTenantName"),
                paypalPlusFacadeFactory::getPaypalPlusFacade);
    }

}