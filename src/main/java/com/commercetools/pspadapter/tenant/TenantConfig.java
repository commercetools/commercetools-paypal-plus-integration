package com.commercetools.pspadapter.tenant;

import com.commercetools.pspadapter.util.CtpClientConfigurationUtils;
import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;

import javax.annotation.Nonnull;

/**
 * Stores all the configs related to one tenant
 */
public class TenantConfig {

    private final String ctpProjectKey;
    private final String ctpClientId;
    private final String ctpClientSecret;

    private final String pPlusClientId;
    private final String pPlusClientSecret;
    private final String pPlusClientMode;

    public TenantConfig(@Nonnull String ctpProjectKey,
                        @Nonnull String ctpClientId,
                        @Nonnull String ctpClientSecret,
                        @Nonnull String pPlusClientId,
                        @Nonnull String pPlusClientSecret,
                        @Nonnull String pPlusClientMode) {
        this.ctpProjectKey = ctpProjectKey;
        this.ctpClientId = ctpClientId;
        this.ctpClientSecret = ctpClientSecret;
        this.pPlusClientId = pPlusClientId;
        this.pPlusClientSecret = pPlusClientSecret;
        this.pPlusClientMode = pPlusClientMode;
    }

    public String getCtpProjectKey() {
        return ctpProjectKey;
    }

    public String getCtpClientId() {
        return ctpClientId;
    }

    public String getCtpClientSecret() {
        return ctpClientSecret;
    }

    public String getPPlusClientId() {
        return pPlusClientId;
    }

    public String getPPlusClientSecret() {
        return pPlusClientSecret;
    }

    public String getPPlusClientMode() {
        return pPlusClientMode;
    }

    public SphereClient createSphereClient () {
        return CtpClientConfigurationUtils.createClient(createCtpConfig());
    }

    private SphereClientConfig createCtpConfig() {
        return SphereClientConfig.of(
                this.ctpProjectKey,
                this.ctpClientId,
                this.ctpClientSecret);
    }

    public APIContext createAPIContext() {
        return new APIContext(this.pPlusClientId, this.pPlusClientSecret, this.pPlusClientMode);
    }
}