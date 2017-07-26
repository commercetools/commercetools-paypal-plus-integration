package com.commercetools.pspadapter.util;

import com.commercetools.testUtil.mockObjects.SphereClientUtils;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class ClientConfigurationUtilsIT {

    @Test
    public void createClient_WithSameConfig_ReturnsDifferentClient() {
        assertThat(CtpClientConfigurationUtils.createClient(SphereClientUtils.CTP_SOURCE_CLIENT_CONFIG))
            .isNotEqualTo(CtpClientConfigurationUtils.createClient(SphereClientUtils.CTP_SOURCE_CLIENT_CONFIG));
    }

    @Test
    public void createClient_WithDifferentConfig_ReturnDifferentClient() {
        assertThat(CtpClientConfigurationUtils.createClient(SphereClientUtils.CTP_SOURCE_CLIENT_CONFIG))
            .isNotEqualTo(CtpClientConfigurationUtils.createClient(SphereClientUtils.CTP_TARGET_CLIENT_CONFIG));
    }
}
