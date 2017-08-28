package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
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
import org.springframework.test.web.servlet.MockMvc;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.createCartAndPayment;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
public class CommercetoolsPatchPaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    private TenantConfig tenantConfig;
    private SphereClient sphereClient;
    private CtpFacade ctpFacade;
    private PaypalPlusFacade ppFacade;

    @Before
    public void setUp() throws Exception {
        tenantConfig = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME)
                .orElseThrow(IllegalStateException::new);

        ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();
        ppFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();

        sphereClient = tenantConfig.createSphereClient();
    }

    @Test
    public void shouldReturnNewPaypalPaymentId() throws Exception {
        final String ctpPaymentId = createCartAndPayment(sphereClient);
        this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)));

        this.mockMvc.perform(post(format("/%s/commercetools/patch/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andExpect(status().isOk())
                .andReturn();
        Payment ctpPayment = executeBlocking(ctpFacade.getPaymentService().getById(ctpPaymentId)).get();
        assertThat(ctpPayment.getInterfaceInteractions().size()).isEqualTo(4);

        // A bug from Paypal makes this test sometimes fails, sometimes not
        // until it's fixed, it will be disabled
//        com.paypal.api.payments.Payment ppPayment = executeBlocking(ppFacade.getPaymentService().lookUp(ctpPayment.getInterfaceId()));
//        assertThat(ppPayment.getTransactions().get(0).getItemList().getShippingAddress()).isNotNull();
    }


}