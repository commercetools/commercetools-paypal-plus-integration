package com.commercetools.payment.handler;

import com.commercetools.payment.BasePaymentUT;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import org.apache.http.HttpResponse;
import org.bitbucket.radistao.test.annotation.AfterAllMethods;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.testUtil.HttpTestUtil.executePostRequest;
import static com.commercetools.testUtil.HttpTestUtil.getContentAsJsonObject;
import static com.commercetools.testUtil.PaypalPlusTestUtil.assertApprovalUrl;
import static com.commercetools.testUtil.ServiceConstants.CREATE_PAYMENT_URL_PATTERN;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.apache.http.HttpStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BeforeAfterSpringTestRunner.class)
public class CommercetoolsCreatePaymentsControllerUT extends BasePaymentUT {

    @BeforeAllMethods
    @Override
    public void setupBeforeAll() {
        super.setupBeforeAll();
    }

    @AfterAllMethods
    @Override
    public void tearDownAfterAll() {
        super.tearDownAfterAll();
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
    }

    /**
     * Verify return code and approvalUrl for successfully created payment, like in doc:
     * https://github.com/commercetools/commercetools-paypal-plus-integration#http-responses
     * <p>
     * Also, verify the payment custom fields have expected values.
     */
    @Test
    public void paypalPlusPaymentIsCreatedSuccessfully() throws Exception {
        final String paymentId = createCartAndPayment(sphereClient);

        final HttpResponse response = executePostRequest(CREATE_PAYMENT_URL_PATTERN, tenantKey, paymentId);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(SC_CREATED);
        assertThat(response.getEntity().getContentType().getValue())
                .startsWith(APPLICATION_JSON);

        final JSONObject content = getContentAsJsonObject(response);
        final String approvalUrl = content.getString("approvalUrl");

        assertApprovalUrl(approvalUrl);

        // verify CTP payment values: approval url + interfaceId + interface interaction
        Payment updatedPayment = sphereClient.executeBlocking(PaymentByIdGet.of(paymentId));
        assertThat(updatedPayment).isNotNull();

        assertThat(updatedPayment.getCustom().getFieldAsString(APPROVAL_URL)).isEqualTo(approvalUrl);
        assertThat(updatedPayment.getPaymentMethodInfo().getMethod()).isEqualTo("default");

        String ppPaymentId = updatedPayment.getInterfaceId();
        assertThat(ppPaymentId).isNotNull();
    }

    @Test
    public void whenPaymentIsMissing_shouldReturn4xxError() throws Exception {
        assertThat(executePostRequest(CREATE_PAYMENT_URL_PATTERN, tenantKey, "nonUUIDString").getStatusLine().getStatusCode()).isEqualTo(SC_BAD_REQUEST);
        assertThat(executePostRequest(CREATE_PAYMENT_URL_PATTERN, tenantKey, UUID.randomUUID().toString()).getStatusLine().getStatusCode()).isEqualTo(SC_NOT_FOUND);
    }

    @Test
    public void whenTenantNameIsWrong_shouldReturn4xxError() throws Exception {
        assertThat(executePostRequest(CREATE_PAYMENT_URL_PATTERN, "dummyTenantKey", UUID.randomUUID().toString()).getStatusLine().getStatusCode()).isEqualTo(SC_NOT_FOUND);
    }

    // TODO: move more tests from integration CommercetoolsCreatePaymentsControllerIT here!!!

}
