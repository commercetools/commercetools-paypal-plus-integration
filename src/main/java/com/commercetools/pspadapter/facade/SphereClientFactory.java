package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;

import javax.annotation.Nonnull;

public interface SphereClientFactory {

    SphereClient createSphereClient(@Nonnull TenantConfig tenantConfig);

    SphereClient createSphereClient(@Nonnull SphereClientConfig clientConfig);
}
