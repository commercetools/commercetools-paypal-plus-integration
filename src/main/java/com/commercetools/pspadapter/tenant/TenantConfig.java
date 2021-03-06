package com.commercetools.pspadapter.tenant;

import com.commercetools.pspadapter.ExtendedAPIContextFactory;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.models.Base;
import io.sphere.sdk.models.SdkDefaults;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import javax.annotation.Nonnull;

/**
 * Stores all the configs related to one tenant.
 * <p>
 * Note: extending {@link Base} is necessary since we want to cache services factories bases on tenant configs.
 */
public class TenantConfig extends Base {

    private final String tenantName;

    private final String ctpProjectKey;
    private final String ctpClientId;
    private final String ctpClientSecret;
    private final SphereClientConfig sphereClientConfig;

    private final String pPlusClientId;
    private final String pPlusClientSecret;
    private final String pPlusClientMode;
    private final ExtendedAPIContextFactory pPlusExtendedApiContextFactory;

    public TenantConfig(@Nonnull String tenantName,
                        @Nonnull String ctpProjectKey,
                        @Nonnull String ctpClientId,
                        @Nonnull String ctpClientSecret,
                        @Nonnull String pPlusClientId,
                        @Nonnull String pPlusClientSecret,
                        @Nonnull String pPlusClientMode) {
        this.tenantName = tenantName;
        this.ctpProjectKey = ctpProjectKey;
        this.ctpClientId = ctpClientId;
        this.ctpClientSecret = ctpClientSecret;
        this.sphereClientConfig = SphereClientConfig.of(ctpProjectKey, ctpClientId, ctpClientSecret);

        this.pPlusClientId = pPlusClientId;
        this.pPlusClientSecret = pPlusClientSecret;
        this.pPlusClientMode = pPlusClientMode;
        this.pPlusExtendedApiContextFactory = new ExtendedAPIContextFactory(this.pPlusClientId, this.pPlusClientSecret, this.pPlusClientMode, this.tenantName);
    }

    public String getTenantName() {
        return tenantName;
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

    public SphereClientConfig getSphereClientConfig() {
        return sphereClientConfig;
    }

    public ExtendedAPIContextFactory getAPIContextFactory() {
        return pPlusExtendedApiContextFactory;
    }

    static final String[] EXCLUDE_FIELD_NAMES = {"ctpClientSecret", "sphereClientConfig", "pPlusClientSecret", "pPlusExtendedApiContextFactory"};

    /**
     * @return String representation of the instance <b>excluding</b> secret values, like {@link #ctpClientSecret}
     * and {@link #pPlusClientSecret}
     */
    @Override
    public String toString() {
        return (new ReflectionToStringBuilder(this, SdkDefaults.TO_STRING_STYLE))
                .setExcludeFieldNames(EXCLUDE_FIELD_NAMES)
                .build();
    }
}