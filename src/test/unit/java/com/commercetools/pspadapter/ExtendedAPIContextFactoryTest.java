package com.commercetools.pspadapter;


import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import org.junit.Test;

import static com.paypal.base.Constants.SANDBOX;
import static org.assertj.core.api.Assertions.assertThat;

public class ExtendedAPIContextFactoryTest {

    @Test
    public void createApiContext_isNotCached() throws Exception {
        ExtendedAPIContextFactory extendedApiContextFactory = new ExtendedAPIContextFactory("a", "b", SANDBOX, "testTenant");
        APIContext apiContext1 = extendedApiContextFactory.createAPIContext().getApiContext();

        assertThat(apiContext1).isNotNull();
        assertThat(apiContext1.getClientID()).isEqualTo("a");
        assertThat(apiContext1.getClientSecret()).isEqualTo("b");
        assertThat(apiContext1.getConfiguration(Constants.MODE)).isEqualTo(SANDBOX);

        APIContext apiContext2 = extendedApiContextFactory.createAPIContext().getApiContext();
        assertThat(apiContext1).isNotSameAs(apiContext2);
    }

    @Test
    public void typeHasProperEqualsAndHashMethodsImplementation() throws Exception {
        ExtendedAPIContextFactory extendedApiContextFactory1 = new ExtendedAPIContextFactory("aaaa", "bbbb", SANDBOX, "testTenant");
        ExtendedAPIContextFactory extendedApiContextFactory_same = new ExtendedAPIContextFactory("aaaa", "bbbb", SANDBOX, "testTenant");
        ExtendedAPIContextFactory extendedApiContextFactory_differentId = new ExtendedAPIContextFactory("ccc", "bbbb", SANDBOX, "testTenant");
        ExtendedAPIContextFactory extendedApiContextFactory_differentSecret = new ExtendedAPIContextFactory("aaaa", "fff", SANDBOX,
                "testTenant");
        ExtendedAPIContextFactory extendedApiContextFactory_differentMode = new ExtendedAPIContextFactory("aaaa", "bbbb",
                "testMode", "testTenant");

        assertThat(extendedApiContextFactory1).isEqualTo(extendedApiContextFactory_same);
        assertThat(extendedApiContextFactory1.hashCode()).isEqualTo(extendedApiContextFactory_same.hashCode());

        assertThat(extendedApiContextFactory1).isNotEqualTo(extendedApiContextFactory_differentId);
        assertThat(extendedApiContextFactory1).isNotEqualTo(extendedApiContextFactory_differentSecret);
        assertThat(extendedApiContextFactory1).isNotEqualTo(extendedApiContextFactory_differentMode);
    }
}