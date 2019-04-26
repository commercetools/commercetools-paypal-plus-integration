package com.commercetools.pspadapter.util;

import com.paypal.base.rest.APIContext;
import io.sphere.sdk.models.Base;

public class ExtendedAPIContext extends Base {

    private final String pPlusClientId;
    private final String pPlusClientSecret;
    private final String pPlusClientMode;
    private final String tenantName;

    public ExtendedAPIContext(String pPlusClientId, String pPlusClientSecret, String pPlusClientMode, String tenantName) {
        this.pPlusClientId = pPlusClientId;
        this.pPlusClientSecret = pPlusClientSecret;
        this.pPlusClientMode = pPlusClientMode;
        this.tenantName = tenantName;
    }

    public APIContext getApiContext() {
        return new APIContext(pPlusClientId, pPlusClientSecret, pPlusClientMode);
    }

    public String getTenantName() {
        return this.tenantName;
    }

}
