package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.PaymentIntegrationTest;
import io.sphere.sdk.payments.Payment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static com.commercetools.payment.constants.Psp.PSP_NAME;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsGetPaymentsControllerIT extends PaymentIntegrationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void onPaypalPaymentNotFound_shouldReturn404() throws Exception {
        mockMvcAsync.performAsync(get(format("/%s/%s/payments/%s/", MAIN_TEST_TENANT_NAME, PSP_NAME, "nonExistingId")))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void onValidPaymentId_shouldReturnPaypalPayment() throws Exception {
        final String ctpPaymentId = createCartAndPayment(sphereClient);
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andDo(print())
                .andReturn();

        Optional<Payment> paymentOpt = executeBlocking(ctpFacade.getPaymentService().getById(ctpPaymentId));

        String paypalPaymentId = paymentOpt.get().getInterfaceId();
        mockMvcAsync.performAsync(get(format("/%s/%s/payments/%s/", MAIN_TEST_TENANT_NAME, PSP_NAME, paypalPaymentId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.payment.id").value(equalTo(paypalPaymentId)))
                .andReturn();
    }
}