package com.commercetools.service.impl;

import io.sphere.sdk.client.SphereClient;

import javax.annotation.Nonnull;

abstract public class BaseSphereService {

    @Nonnull
    final SphereClient sphereClient;

    protected BaseSphereService(@Nonnull SphereClient sphereClient) {
        this.sphereClient = sphereClient;
    }
}
