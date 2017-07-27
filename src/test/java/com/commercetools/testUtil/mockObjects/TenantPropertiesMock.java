package com.commercetools.testUtil.mockObjects;

import com.commercetools.pspadapter.tenant.TenantProperties;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TenantPropertiesMock {

    public static TenantProperties setUpMockTenantProperties(String... tenantNames) {
        List<TenantProperties.Tenant> tenants = Arrays.stream(tenantNames)
                .map(TenantPropertiesMock::createTenant)
                .collect(Collectors.toList());

        TenantProperties tenantProperties = new TenantProperties();
        tenantProperties.setTenants(tenants);
        return tenantProperties;
    }

    private static TenantProperties.Tenant createTenant(String tenantName) {
        TenantProperties.Tenant.Ctp ctp = new TenantProperties.Tenant.Ctp();
        ctp.setClientId("testClientId");
        ctp.setClientSecret("testClientSecret");
        ctp.setProjectKey(tenantName);

        TenantProperties.Tenant.PaypalPlus paypalPlus = new TenantProperties.Tenant.PaypalPlus();
        paypalPlus.setId("ppId");
        paypalPlus.setMode("sandbox");
        paypalPlus.setSecret("ppSecret");

        TenantProperties.Tenant tenant1 = new TenantProperties.Tenant();
        tenant1.setName(tenantName);
        tenant1.setCtp(ctp);
        tenant1.setPaypalPlus(paypalPlus);
        return tenant1;
    }
}