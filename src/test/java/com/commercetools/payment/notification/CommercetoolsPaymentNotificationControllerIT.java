package com.commercetools.payment.notification;

import com.commercetools.Application;
import com.commercetools.payment.PaymentIntegrationTest;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionDraft;
import io.sphere.sdk.payments.TransactionDraftBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.PaymentUpdateCommand;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static io.sphere.sdk.models.DefaultCurrencyUnits.USD;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
public class CommercetoolsPaymentNotificationControllerIT extends PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value(value = "classpath:mockData/notification/paymentSaleCompletedResponse.json")
    private Resource paymentSaleCompletedResponseResource;

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

        sphereClient = tenantConfig.createSphereClient();
    }

    @Test
    public void handlePaymentsIgnoresTrailingSlash() throws Exception {
        this.mockMvc.perform(post("/blah-blah/paypalplus/notification/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isOk());

        this.mockMvc.perform(post("/blah-blah/paypalplus/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void onCompletedNotification_shouldUpdateTransactionState() throws Exception {
        String interfaceId = getMockPaymentInterfaceId();

        this.mockMvc.perform(post(format("/%s/paypalplus/notification", MAIN_TEST_TENANT_NAME))
                .content(getPaymentSaleCompletedResponseMock(interfaceId))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Payment payment = executeBlocking(ctpFacade.getPaymentService()
                .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, interfaceId))
                .get();

        Transaction transaction = payment.getTransactions().get(0);
        assertThat(transaction.getType()).isEqualTo(TransactionType.CHARGE);
        assertThat(transaction.getState()).isEqualTo(TransactionState.SUCCESS);
    }

    private String getMockPaymentInterfaceId() throws Exception {
        String paymentId = createCartAndPayment(sphereClient);

        this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)));

        Payment payment = executeBlocking(this.sphereClient.execute(PaymentByIdGet.of(paymentId)));

        TransactionDraft draft = TransactionDraftBuilder.of(TransactionType.CHARGE, Money.of(10, USD))
                .state(TransactionState.PENDING).build();
        Payment updatedPayment = executeBlocking(sphereClient.execute(PaymentUpdateCommand.of(payment, AddTransaction.of(draft))));

        return updatedPayment.getInterfaceId();
    }

    public String getPaymentSaleCompletedResponseMock(String paypalPaymentId) throws IOException {
        return new BufferedReader(new InputStreamReader(paymentSaleCompletedResponseResource.getInputStream()))
                .lines()
                .collect(Collectors.joining())
                .replaceAll("PAY-xxxxxx", paypalPaymentId);
    }
}