package com.commercetools.testUtil.mockObjects;

import io.sphere.sdk.client.SphereClientConfig;

public class SphereClientUtils {

    public static final SphereClientConfig CTP_SOURCE_CLIENT_CONFIG = SphereClientConfig.of(
        "SOURCE_PROJECT_KEY",
        "SOURCE_CLIENT_ID",
        "SOURCE_CLIENT_SECRET");
    public static final SphereClientConfig CTP_TARGET_CLIENT_CONFIG = SphereClientConfig.of(
        "TARGET_PROJECT_KEY",
        "TARGET_CLIENT_ID",
        "TARGET_CLIENT_SECRET");

}

