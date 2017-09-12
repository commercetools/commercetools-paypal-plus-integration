package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.PaymentIntegrationTest;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.test.web.servlet.MockMvcAsync;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.util.CustomFieldUtil.getCustomFieldStringOrEmpty;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
// completely wipe-out CTP project Payment, Cart, Order endpoints before the test cases
public class CommercetoolsCreatePaymentsControllerIT extends PaymentIntegrationTest {

    @Autowired
    private MockMvcAsync mockMvcAsync;

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    private TenantConfig tenantConfig;
    private SphereClient sphereClient;
    private CtpFacade ctpFacade;

    @Before
    public void setUp() throws Exception {
        tenantConfig = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME)
                .orElseThrow(IllegalStateException::new);

        ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();

        sphereClient = tenantConfig.createSphereClient();
    }

    @Test
    public void shouldReturnNewPaypalPaymentId() throws Exception {
        final String ctpPaymentId = createCartAndPayment(sphereClient);
        MvcResult mvcResult = mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

        // validate response json: approvalUrl
        String returnedApprovalUrl = verifyApprovalUrl(mvcResult);

        // verify CTP payment values: approval url + interfaceId + interface interaction
        Payment updatedPayment = executeBlocking(ctpFacade.getPaymentService().getById(ctpPaymentId)).orElse(null);
        assertThat(updatedPayment).isNotNull();

        assertThat(getCustomFieldStringOrEmpty(updatedPayment, APPROVAL_URL)).isEqualTo(returnedApprovalUrl);

        assertThat(updatedPayment.getPaymentMethodInfo().getMethod()).isEqualTo(DEFAULT);

        String ppPaymentId = updatedPayment.getInterfaceId();
        assertThat(ppPaymentId).isNotNull();

        assertInterfaceInteractions(ctpPaymentId, sphereClient);

        com.paypal.api.payments.Payment createdPpPayment = getPpPayment(tenantConfig, ppPaymentId);

        assertCustomFields(createdPpPayment, returnedApprovalUrl, ppPaymentId);

        assertThat(createdPpPayment.getPayer()).isNull();
    }
}
