package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.tenant.TenantProperties;

import java.util.Collections;

public class TenantPropertiesMock {

    public static TenantProperties setUpMockTenantProperties(String tenantName) {
        TenantProperties.Tenant.Ctp ctp = new TenantProperties.Tenant.Ctp();
        ctp.setClientId("testClientId");
        ctp.setClientSecret("testClientSecret");
        ctp.setProjectKey("testProjectKey");

        TenantProperties.Tenant.PaypalPlus paypalPlus = new TenantProperties.Tenant.PaypalPlus();
        paypalPlus.setId("ppId");
        paypalPlus.setMode("sandbox");
        paypalPlus.setSecret("ppSecret");

        TenantProperties.Tenant tenant = new TenantProperties.Tenant();
        tenant.setName(tenantName);
        tenant.setCtp(ctp);
        tenant.setPaypalPlus(paypalPlus);

        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setTenants(Collections.singletonList(tenant));
        return tenantProperties;
    }
}