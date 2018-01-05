package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.tenant.TenantConfig;
import io.sphere.sdk.client.SphereClientConfig;

import javax.annotation.Nonnull;

import static com.paypal.base.Constants.SANDBOX;

public final class TenantConfigMockUtil {

    @Nonnull
    public static TenantConfig getMockTenantConfig() {
        return new TenantConfig("mockTenantName", "mockTenantCtpKey", "mockTenantCtpId", "mockTenantCtpSecret",
                "mockTenantPpClientId", "mockTenantPpClientSecret", SANDBOX);
    }

    @Nonnull
    public static TenantConfig getMockTenantConfig(String tenantName) {
        return new TenantConfig(tenantName, "mockTenantCtpKey", "mockTenantCtpId", "mockTenantCtpSecret",
                "mockTenantPpClientId", "mockTenantPpClientSecret", SANDBOX);
    }

    @Nonnull
    public static SphereClientConfig getMockSphereClientConfig() {
        return SphereClientConfig.of("mockTenantCtpKey", "mockTenantCtpId", "mockTenantCtpSecret");
    }

    @Nonnull
    public static SphereClientConfig getMockSphereClientConfig(String projectKey) {
        return SphereClientConfig.of(projectKey, "mockTenantCtpId", "mockTenantCtpSecret");
    }

    private TenantConfigMockUtil() {
    }
}
