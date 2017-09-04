package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.PaymentIntegrationTest;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
public class CommercetoolsExecutePaymentsControllerIT extends PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    private TenantConfig tenantConfig;
    private SphereClient sphereClient;
    private CtpFacade ctpFacade;
    private PaypalPlusFacade paypalPlusFacade;

    @Before
    public void setUp() throws Exception {
        tenantConfig = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME)
                .orElseThrow(IllegalStateException::new);

        ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();
        paypalPlusFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();

        sphereClient = tenantConfig.createSphereClient();
    }

    @Test
    public void finalSlashIsProcessedToo() throws Exception {
        this.mockMvc.perform(post("/asdhfasdfasf/commercetools/execute/payments/6753324-23452-sgsfgd/").content(""))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @Ignore("Bug in Paypal Plus: https://github.com/paypal/PayPal-REST-API-issues/issues/124")
    public void whenPaypalPayerIdIsWrong_shouldPatch_thenShouldReturn400() throws Exception {
        String paymentId = createCartAndPayment(sphereClient);

        this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)));

        Payment payment = executeBlocking(this.sphereClient.execute(PaymentByIdGet.of(paymentId)));

        String interfaceId = payment.getInterfaceId();
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("paypalPlusPayerId", "nonExistingPayerId");
        jsonBody.put("paypalPlusPaymentId", interfaceId);
        this.mockMvc.perform(post(format("/%s/commercetools/execute/payments/", MAIN_TEST_TENANT_NAME))
                .content(jsonBody.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        com.paypal.api.payments.Payment pPPayment = executeBlocking(paypalPlusFacade.getPaymentService().lookUp(interfaceId));
        Optional<Payment> ctpPaymentOpt = executeBlocking(ctpFacade.getPaymentService().getById(paymentId));
        assertThat(pPPayment.getTransactions().get(0).getItemList().getShippingAddress()).isNotNull();
        assertThat(ctpPaymentOpt).isNotEmpty();

        // assert interface interactions
        Payment ctpPayment = ctpPaymentOpt.get();
        assertThat(ctpPayment.getInterfaceInteractions()).hasSize(6);

    }

    @Test
    public void whenPaypalPaymentIdIsNotExisting_shouldReturn404() throws Exception {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("paypalPlusPayerId", "nonExistingPayerId");
        jsonBody.put("paypalPlusPaymentId", UUID.randomUUID().toString());
        this.mockMvc.perform(post(format("/%s/commercetools/execute/payments/", MAIN_TEST_TENANT_NAME))
                .content(jsonBody.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

}