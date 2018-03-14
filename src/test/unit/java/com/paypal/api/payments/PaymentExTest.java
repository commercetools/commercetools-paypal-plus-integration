package com.paypal.api.payments;

import com.paypal.api.ApplicationContext;
import org.junit.Test;

import static com.commercetools.testUtil.JsonAssertUtil.assertJsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PaymentExTest {

    @Test
    public void toJson_createsJsonWithShippingPreference() throws Exception {
        String emptyPayment = new PaymentEx().toJSON();
        assertThat(emptyPayment).doesNotContain("application_context");

        String emptyApplicationContext = new PaymentEx()
                .setApplicationContext(new ApplicationContext()).toJSON();

        assertJsonPath(emptyApplicationContext, "$.application_context", is(notNullValue()));
        assertThat(emptyApplicationContext).doesNotContain("shipping_reference");

        String withShippingPreference = new PaymentEx()
                .setApplicationContext(new ApplicationContext()
                        .setShippingPreference("someShippingPreference"))
                .toJSON();
        assertJsonPath(withShippingPreference, "$.application_context.shipping_preference", is("someShippingPreference"));
    }
}