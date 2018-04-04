package com.commercetools.pspadapter.util;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.projects.Project;
import io.sphere.sdk.projects.queries.ProjectGet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.pspadapter.util.CtpClientConfigurationUtils.createSphereClient;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CtpClientConfigurationUtilsIT {

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Test
    public void createSphereClient_returnsValidInstance() throws Exception {
        TenantConfig config = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME).orElseThrow(IllegalStateException::new);

        SphereClient sphereClient = createSphereClient(SphereClientConfig.of(config.getCtpProjectKey(), config.getCtpClientId(), config.getCtpClientSecret()));

        assertThat(sphereClient).isNotNull();

        Project project = executeBlocking(sphereClient.execute(ProjectGet.of()));
        assertThat(project).isNotNull();
        assertThat(project.getKey()).isEqualTo(config.getCtpProjectKey());
    }

}