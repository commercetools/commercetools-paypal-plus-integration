package com.commercetools.pspadapter.util;

import com.commercetools.pspadapter.APIContextFactory;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import org.junit.Test;

import static com.paypal.base.Constants.SANDBOX;
import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedAPIContextTest {

    @Test
    public void createApiContext_isNotCached() throws Exception {
        APIContextFactory apiContextFactory = new APIContextFactory("a", "b", SANDBOX, "testTenant");
        ExtendedAPIContext extendedAPIContext = apiContextFactory.createAPIContext();
        APIContext apiContext = extendedAPIContext.getApiContext();

        assertThat(apiContext).isNotNull();
        assertThat(apiContext.getClientID()).isEqualTo("a");
        assertThat(apiContext.getClientSecret()).isEqualTo("b");
        assertThat(apiContext.getConfiguration(Constants.MODE)).isEqualTo(SANDBOX);
        assertThat(extendedAPIContext.getTenantName()).isEqualTo("testTenant");

        APIContext apiContext2 = apiContextFactory.createAPIContext().getApiContext();
        assertThat(apiContext).isNotSameAs(apiContext2);
    }

    @Test
    public void typeHasProperEqualsAndHashMethodsImplementation() throws Exception {
        ExtendedAPIContext extendedAPIContext = new ExtendedAPIContext("aaaa", "bbbb", SANDBOX, "testTenant");
        ExtendedAPIContext extendedAPIContext_same = new ExtendedAPIContext("aaaa", "bbbb", SANDBOX, "testTenant");
        ExtendedAPIContext extendedAPIContext_differentId = new ExtendedAPIContext("ccc", "bbbb", SANDBOX, "testTenant");
        ExtendedAPIContext extendedAPIContext_differentSecret = new ExtendedAPIContext("aaaa", "fff", SANDBOX,
                "testTenant");
        ExtendedAPIContext extendedAPIContext_differentMode = new ExtendedAPIContext("aaaa", "bbbb",
                "testMode", "testTenant");
        ExtendedAPIContext extendedAPIContext_differentTenantName = new ExtendedAPIContext("aaaa", "bbbb",
                SANDBOX, "testTenant2");

        assertThat(extendedAPIContext).isEqualTo(extendedAPIContext_same);
        assertThat(extendedAPIContext.hashCode()).isEqualTo(extendedAPIContext_same.hashCode());

        assertThat(extendedAPIContext).isNotEqualTo(extendedAPIContext_differentId);
        assertThat(extendedAPIContext).isNotEqualTo(extendedAPIContext_differentSecret);
        assertThat(extendedAPIContext).isNotEqualTo(extendedAPIContext_differentMode);
        assertThat(extendedAPIContext).isNotEqualTo(extendedAPIContext_differentTenantName);
    }
}