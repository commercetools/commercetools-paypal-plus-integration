package com.commercetools.pspadapter;


import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import org.junit.Test;

import static com.paypal.base.Constants.SANDBOX;
import static org.assertj.core.api.Assertions.assertThat;

public class APIContextFactoryTest {

    @Test
    public void createApiContext_isNotCached() throws Exception {
        APIContextFactory apiContextFactory = new APIContextFactory("a", "b", SANDBOX, "testTenant");
        APIContext apiContext1 = apiContextFactory.createAPIContext().getApiContext();

        assertThat(apiContext1).isNotNull();
        assertThat(apiContext1.getClientID()).isEqualTo("a");
        assertThat(apiContext1.getClientSecret()).isEqualTo("b");
        assertThat(apiContext1.getConfiguration(Constants.MODE)).isEqualTo(SANDBOX);

        APIContext apiContext2 = apiContextFactory.createAPIContext().getApiContext();
        assertThat(apiContext1).isNotSameAs(apiContext2);
    }

    @Test
    public void typeHasProperEqualsAndHashMethodsImplementation() throws Exception {
        APIContextFactory apiContextFactory1 = new APIContextFactory("aaaa", "bbbb", SANDBOX, "testTenant");
        APIContextFactory apiContextFactory_same = new APIContextFactory("aaaa", "bbbb", SANDBOX, "testTenant");
        APIContextFactory apiContextFactory_differentId = new APIContextFactory("ccc", "bbbb", SANDBOX, "testTenant");
        APIContextFactory apiContextFactory_differentSecret = new APIContextFactory("aaaa", "fff", SANDBOX,
                "testTenant");
        APIContextFactory apiContextFactory_differentMode = new APIContextFactory("aaaa", "bbbb",
                "testMode", "testTenant");

        assertThat(apiContextFactory1).isEqualTo(apiContextFactory_same);
        assertThat(apiContextFactory1.hashCode()).isEqualTo(apiContextFactory_same.hashCode());

        assertThat(apiContextFactory1).isNotEqualTo(apiContextFactory_differentId);
        assertThat(apiContextFactory1).isNotEqualTo(apiContextFactory_differentSecret);
        assertThat(apiContextFactory1).isNotEqualTo(apiContextFactory_differentMode);
    }
}