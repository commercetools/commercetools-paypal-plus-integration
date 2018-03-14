package com.commercetools.pspadapter.tenant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.TestConstants.SECOND_TEST_TENANT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TenantConfigFactoryIntegrationTest {

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Test
    public void getTenantConfig_returnsCachedValue() throws Exception {
        assertThat(tenantConfigFactory).isNotNull();
        Optional<TenantConfig> config1 = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME);
        Optional<TenantConfig> config2 = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME);
        Optional<TenantConfig> configDifferent = tenantConfigFactory.getTenantConfig(SECOND_TEST_TENANT_NAME);

        assertThat(config1).isNotEmpty();
        assertThat(config2).isNotEmpty();
        assertThat(configDifferent).isNotEmpty();
        assertThat(config1).containsSame(config2.get());
        assertThat(configDifferent.get()).isNotEqualTo(config1.get());
        assertThat(configDifferent.get()).isNotEqualTo(config2.get());
    }

}