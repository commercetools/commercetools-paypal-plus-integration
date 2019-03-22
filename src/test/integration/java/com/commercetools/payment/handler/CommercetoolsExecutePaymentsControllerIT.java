package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.BasePaymentIT;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.UUID;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsExecutePaymentsControllerIT extends BasePaymentIT {

    @BeforeAllMethods
    @Override
    public void setupBeforeAll() {
        super.setupBeforeAll();
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void finalSlashIsProcessedToo() throws Exception {
        String paymentId = createCartAndPayment(sphereClient);

        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s/", MAIN_TEST_TENANT_NAME, paymentId)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approvalUrl").value(not(empty())))
                .andReturn();

    }

    @Test
    @Ignore("The test is unstable, see bug in Paypal Plus: https://github.com/paypal/PayPal-REST-API-issues/issues/124")
    public void whenPaypalPayerIdIsWrong_shouldReturn400() throws Exception {
        String paymentId = createCartAndPayment(sphereClient);

        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approvalUrl").value(not(empty())))
                .andReturn();

        Payment payment = executeBlocking(this.sphereClient.execute(PaymentByIdGet.of(paymentId)));

        String interfaceId = payment.getInterfaceId();
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("paypalPlusPayerId", "nonExistingPayerId");
        jsonBody.put("paypalPlusPaymentId", interfaceId);
        mockMvcAsync.performAsync(
                post(format("/%s/commercetools/execute/payments/", MAIN_TEST_TENANT_NAME))
                        .content(jsonBody.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(containsString(interfaceId)))
                .andReturn();

        Optional<Payment> ctpPaymentOpt = executeBlocking(ctpFacade.getPaymentService().getById(paymentId));
        assertThat(ctpPaymentOpt).isNotEmpty();

        // assert interface interactions
        Payment ctpPayment = ctpPaymentOpt.get();
        assertThat(ctpPayment.getInterfaceInteractions()).hasSize(4);
    }

    @Test
    public void whenPaypalPaymentIdIsNotExisting_shouldReturn404() throws Exception {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("paypalPlusPayerId", "nonExistingPayerId");
        final String randomUuid = UUID.randomUUID().toString();
        jsonBody.put("paypalPlusPaymentId", randomUuid);

        mockMvcAsync.performAsync(
                post(format("/%s/commercetools/execute/payments/", MAIN_TEST_TENANT_NAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(containsString(randomUuid)))
                .andReturn();
    }

}