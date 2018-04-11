package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.BasePaymentIT;
import com.paypal.api.payments.Payer;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import org.bitbucket.radistao.test.annotation.AfterAllMethods;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Locale;
import java.util.UUID;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.util.CustomFieldUtil.getCustomFieldStringOrEmpty;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsCreatePaymentsControllerIT extends BasePaymentIT {

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

    @Test
    public void finalSlashIsProcessedToo() throws Exception {
        final String paymentId = createCartAndPayment(sphereClient);

        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s/", MAIN_TEST_TENANT_NAME, paymentId)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();
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

        assertThat(ofNullable(createdPpPayment.getPayer())
                .map(Payer::getExternalSelectedFundingInstrumentType)).isEmpty();
    }

    /**
     * Similar to {@link CommercetoolsCreatePaymentsControllerIT#shouldReturnNewPaypalPaymentId()},
     * but verifies other saved payment properties, like payer info,
     */
    @Test
    @Ignore("The test is unstable, see bug in Paypal Plus: https://github.com/paypal/PayPal-REST-API-issues/issues/124, "
            + "https://github.com/paypal/PayPal-REST-API-issues/issues/180"
            + "https://github.com/paypal/PayPal-REST-API-issues/issues/181")
    public void createdPaymentHasExpectedProperties() throws Exception {
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

        // validate other fields of the created payment (expected properties)
        assertThat(createdPpPayment.getPayer()).isNotNull();
        assertThat(createdPpPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);

        // in default payments we don't specify any private payer specific info
        assertThat(createdPpPayment.getPayer().getExternalSelectedFundingInstrumentType()).isNull();
        assertThat(createdPpPayment.getPayer().getPayerInfo()).isNull();
        assertThat(createdPpPayment.getPayer().getFundingInstruments()).isNullOrEmpty();
    }

    @Test
    public void whenPaymentIsMissing_shouldReturn4xxError() throws Exception {
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, "nonUUIDString")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, UUID.randomUUID().toString())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenCartIsMissing_shouldReturn404() throws Exception {
        Payment payment = executeBlocking(createPaymentCS(sphereClient, Money.of(10, EUR), Locale.ENGLISH));
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, payment.getId())))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(containsString(payment.getId())))
                .andReturn();
    }

    @Test
    public void whenPaymentInterfaceIsIncorrect_shouldReturn400() throws Exception {
        PaymentDraftDsl dsl = createPaymentDraftBuilder(Money.of(10, EUR), Locale.ENGLISH)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface("NOT-PAYPAL-INTERFACE").method(DEFAULT).build())
                .build();

        Cart cart = executeBlocking(createCartCS(sphereClient)
                .thenCompose(c -> sphereClient.execute(PaymentCreateCommand.of(dsl))
                        .thenApply(payment -> new CtpPaymentWithCart(payment, c))
                        .thenCompose(ctpPaymentWithCart -> sphereClient.execute(CartUpdateCommand.of(ctpPaymentWithCart.getCart(),
                                AddPayment.of(ctpPaymentWithCart.getPayment()))))));

        String paymentId = cart.getPaymentInfo().getPayments().get(0).getId();

        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value(allOf(
                        containsString(paymentId),
                        containsString("has incorrect payment interface"),
                        containsString("NOT-PAYPAL-INTERFACE"))))
                .andReturn();
    }
}
