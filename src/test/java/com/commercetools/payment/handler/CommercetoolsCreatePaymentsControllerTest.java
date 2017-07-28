package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
// completely wipe-out CTP project Payment, Cart, Order endpoints before the test cases
public class CommercetoolsCreatePaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    private SphereClient sphereClient;

    @Before
    public void setUp() {
        sphereClient = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME)
                .map(TenantConfig::createSphereClient).orElse(null);
    }

    @Test
    public void finalSlashIsProcessedToo() throws Exception {
        this.mockMvc.perform(get("/asdhfasdfasf/commercetools/create/payments/6753324-23452-sgsfgd/"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldReturnNewPaypalPaymentId() throws Exception {
        //createCartAndPayment();
        // TODO: akovalenko - complete the test
//        this.mockMvc.perform(get("/" + MAIN_TEST_TENANT_NAME + "/commercetools/create/payments/XXX-YYY"))
//                .andDo(print())
//                .andExpect(status().isOk());
    }

//    private void createCartAndPayment() {
//        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
//                .currency(USD)
//                .build();
//
//        executeBlocking(sphereClient.execute(CartCreateCommand.of(dummyComplexCartWithDiscounts)));
//    }

}
