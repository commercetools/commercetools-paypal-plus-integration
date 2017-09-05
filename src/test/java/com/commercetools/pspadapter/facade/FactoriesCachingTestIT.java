package com.commercetools.pspadapter.facade;

import com.commercetools.Application;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.TestConstants.SECOND_TEST_TENANT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Some classes beans, like {@link TenantConfigFactory} and {@link PaypalPlusFacadeFactory} are implemented with spring
 * caching. This set of tests verifies the caching works as expected.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class FactoriesCachingTestIT {

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Autowired
    private PaypalPlusFacadeFactory paypalPlusFacadeFactory;

    /**
     * Multiple call of tenantConfigFactory.getTenantConfig(config) should return the same instance
     */
    @Test
    public void getTenantConfig_shouldCacheResults() {
        //
        TenantConfig tenantConfig1_1 = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);
        TenantConfig tenantConfig1_2 = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);
        assertThat(tenantConfig1_1).isSameAs(tenantConfig1_2);

        TenantConfig tenantConfig2_1 = tenantConfigFactory.getTenantConfig(SECOND_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);
        TenantConfig tenantConfig2_2 = tenantConfigFactory.getTenantConfig(SECOND_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);
        assertThat(tenantConfig2_1).isSameAs(tenantConfig2_2);
    }

    /**
     * Multiple call of paypalPlusFacadeFactory.getPaypalPlusFacade() should return the same instance
     */
    @Test
    public void getPaypalPlusFacade_shouldCacheResults() {
        TenantConfig tenantConfig1 = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);
        PaypalPlusFacade paypalPlusFacade1_1 = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig1);
        PaypalPlusFacade paypalPlusFacade1_2 = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig1);
        assertThat(paypalPlusFacade1_1).isSameAs(paypalPlusFacade1_2);

        TenantConfig tenantConfig2 = tenantConfigFactory.getTenantConfig(SECOND_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);
        PaypalPlusFacade paypalPlusFacade2_1 = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig2);
        PaypalPlusFacade paypalPlusFacade2_2 = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig2);
        assertThat(paypalPlusFacade2_1).isNotEqualTo(paypalPlusFacade1_1);
        assertThat(paypalPlusFacade2_1).isSameAs(paypalPlusFacade2_2);

        // test case when TenantConfig is a different instance, but with equal fields:
        // should still return the same PaypalPlusFacade instance
        TenantConfig tenantConfig2Clone = new TenantConfig(tenantConfig2.getTenantName(),
                tenantConfig2.getCtpProjectKey(), tenantConfig2.getCtpClientId(), tenantConfig2.getCtpClientSecret(),
                tenantConfig2.getPPlusClientId(), tenantConfig2.getPPlusClientSecret(), tenantConfig2.getPPlusClientMode());

        assertThat(tenantConfig2).isNotSameAs(tenantConfig2Clone);
        assertThat(tenantConfig2).isEqualTo(tenantConfig2Clone);

        PaypalPlusFacade paypalPlusFacade2Clone = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig2Clone);
        assertThat(paypalPlusFacade2Clone).isSameAs(paypalPlusFacade2_1);
        assertThat(paypalPlusFacade2Clone).isSameAs(paypalPlusFacade2_2);

    }
}