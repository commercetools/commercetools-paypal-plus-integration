package com.commercetools.payment.notification;

import com.commercetools.Application;
import com.commercetools.payment.BasePaymentIT;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import com.commercetools.testUtil.customTestConfigs.ServiceConfig;
import io.sphere.sdk.payments.*;
import io.sphere.sdk.payments.commands.PaymentUpdateCommand;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

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
@ContextConfiguration(classes = ServiceConfig.class)
public class CommercetoolsPaymentNotificationControllerIT extends BasePaymentIT {

    private static final String INTERACTION_ID = "testInteractionId";

    @Value(value = "classpath:mockData/notification/paymentSaleCompletedResponse.json")
    private Resource paymentSaleCompletedResponseResource;

    @Value(value = "classpath:mockData/notification/fakeNotificationResponse.json")
    private Resource fakeNotificationResponseResource;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void handlePaymentsIgnoresTrailingSlash() throws Exception {
        String interfaceId = getMockPaymentInterfaceId(INTERACTION_ID);

        mockMvcAsync.performAsync(post(format("/%s/paypalplus/notification", MAIN_TEST_TENANT_NAME))
                .content(getPaymentSaleCompletedResponseMock(interfaceId, INTERACTION_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvcAsync.performAsync(post(format("/%s/paypalplus/notification/", MAIN_TEST_TENANT_NAME))
                .content(getPaymentSaleCompletedResponseMock(interfaceId, INTERACTION_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void onCompletedNotification_shouldUpdateTransactionState() throws Exception {
        String interfaceId = getMockPaymentInterfaceId(INTERACTION_ID);

        mockMvcAsync.performAsync(post(format("/%s/paypalplus/notification", MAIN_TEST_TENANT_NAME))
                .content(getPaymentSaleCompletedResponseMock(interfaceId, INTERACTION_ID))
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

    @Test
    public void onUnknownNotification_shouldSaveToInterfaceInteraction() throws Exception {
        String interfaceId = getMockPaymentInterfaceId(INTERACTION_ID);

        Payment paymentBefore = executeBlocking(ctpFacade.getPaymentService()
                .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, interfaceId))
                .get();

        mockMvcAsync.performAsync(post(format("/%s/paypalplus/notification", MAIN_TEST_TENANT_NAME))
                .content(getFakeNotificationEventResponseMock(interfaceId))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        Payment paymentAfter = executeBlocking(ctpFacade.getPaymentService()
                .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, interfaceId))
                .get();

        Transaction transactionBefore = paymentBefore.getTransactions().get(0);
        Transaction transactionAfter = paymentAfter.getTransactions().get(0);
        assertThat(transactionAfter.getType()).isEqualTo(transactionBefore.getType());
        assertThat(transactionAfter.getState()).isEqualTo(transactionBefore.getState());
        assertThat(paymentAfter.getInterfaceInteractions().size()).isEqualTo(paymentBefore.getInterfaceInteractions().size() + 1);
    }

    private String getMockPaymentInterfaceId(String transactionInteractionId) throws Exception {
        String paymentId = createCartAndPayment(sphereClient);

        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)));

        Payment payment = executeBlocking(this.sphereClient.execute(PaymentByIdGet.of(paymentId)));

        TransactionDraft draft = TransactionDraftBuilder.of(TransactionType.CHARGE, Money.of(10, USD))
                .state(TransactionState.PENDING)
                .interactionId(transactionInteractionId)
                .build();
        Payment updatedPayment = executeBlocking(sphereClient.execute(PaymentUpdateCommand.of(payment, AddTransaction.of(draft))));

        return updatedPayment.getInterfaceId();
    }

    public String getFakeNotificationEventResponseMock(String paypalPaymentId) throws IOException {
        return new BufferedReader(new InputStreamReader(fakeNotificationResponseResource.getInputStream()))
                .lines()
                .collect(Collectors.joining())
                .replaceAll("PAY-xxxxxx", paypalPaymentId);
    }

    public String getPaymentSaleCompletedResponseMock(String paypalPaymentId, String resourceId) throws IOException {
        return new BufferedReader(new InputStreamReader(paymentSaleCompletedResponseResource.getInputStream()))
                .lines()
                .collect(Collectors.joining())
                .replaceAll("PAY-xxxxxx", paypalPaymentId)
                .replaceAll("replaceResourceId", resourceId);
    }
}