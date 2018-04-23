package com.commercetools.testUtil.ctpUtil;

import com.commercetools.pspadapter.tenant.TenantProperties;
import io.sphere.sdk.client.BlockingSphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.client.SphereClientFactory;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static java.util.Map.Entry.comparingByKey;

/**
 * Util to create sphere client from different configs for usecase tests.
 */
public final class SphereClientTestUtil {

    /**
     * Get first (by alphabetical key name) tenant config for the integration tests from supplied tenant properties.
     * @param tenantProperties tenant properties read from Spring configuration.
     * @return first tenant config, where key is the tenant name, value - tenant properties.
     */
    public static Map.Entry<String, TenantProperties.Tenant> getFirstTenantEntry(@Nonnull TenantProperties tenantProperties) {
        return tenantProperties.getTenants().entrySet().stream()
                .min(comparingByKey()) // find first tenant by key in alphabetical order
                .orElseThrow(() -> new IllegalStateException("Can't find CTP config in tenantProperties"));
    }

    /**
     * @param tenant tenant config from which to create sphere client.
     * @return new instance of blocking sphere client. <b>Don't </b>
     */
    public static BlockingSphereClient getBlockingSphereClient(@Nonnull TenantProperties.Tenant tenant) {
        TenantProperties.Tenant.Ctp ctpConfig = tenant.getCtp();
        return BlockingSphereClient.of(
                SphereClientFactory.of().createClient(SphereClientConfig.of(ctpConfig.getProjectKey(), ctpConfig.getClientId(), ctpConfig.getClientSecret())),
                ofSeconds(30));
    }

    private SphereClientTestUtil() {
    }
}
