package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.BasePaymentIT;
import com.commercetools.payment.constants.ctp.CtpPaymentMethods;
import com.paypal.api.payments.Address;
import com.paypal.api.payments.PayerInfo;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import org.bitbucket.radistao.test.annotation.AfterAllMethods;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.Locale;

import static com.commercetools.helper.mapper.impl.payment.InstallmentPaymentMapperImpl.CREDIT;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.INSTALLMENT;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
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

/**
 * This test is similar to  {@link CommercetoolsCreatePaymentsControllerIT},
 * but tests {@link CtpPaymentMethods#INSTALLMENT} payment creation.
 */
@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsCreatePaymentInstallmentIT extends BasePaymentIT {

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

    @Override
    protected PaymentDraftBuilder createPaymentDraftBuilder(@Nonnull MonetaryAmount totalPrice, @Nullable Locale locale) {
        return super.createPaymentDraftBuilder(totalPrice, locale)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of()
                        .paymentInterface(PAYPAL_PLUS)
                        .method(INSTALLMENT)
                        .build());
    }

    /**
     * Similar to {@link CommercetoolsCreatePaymentsControllerIT#shouldReturnNewPaypalPaymentId()},
     * but for {@link CtpPaymentMethods#INSTALLMENT} payments.
     */
    @Test
    @Ignore("The test is unstable, see bug in Paypal Plus: https://github.com/paypal/PayPal-REST-API-issues/issues/124")
    public void installmentPaymentCreated() throws Exception {
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

        assertThat(updatedPayment.getPaymentMethodInfo().getMethod()).isEqualTo(INSTALLMENT);

        String ppPaymentId = updatedPayment.getInterfaceId();
        assertThat(ppPaymentId).isNotNull();

        assertInterfaceInteractions(ctpPaymentId, sphereClient);

        com.paypal.api.payments.Payment createdPpPayment = getPpPayment(tenantConfig, ppPaymentId);

        assertCustomFields(createdPpPayment, returnedApprovalUrl, ppPaymentId);

        // opposite to default payment - installment should have ExternalSelectedFundingInstrumentType == CREDIT
        // and payer info with billing address
        assertThat(createdPpPayment.getPayer()).isNotNull();
        assertThat(createdPpPayment.getPayer().getExternalSelectedFundingInstrumentType()).isEqualTo(CREDIT);
        PayerInfo payerInfo = createdPpPayment.getPayer().getPayerInfo();
        assertThat(payerInfo).isNotNull();
        assertThat(payerInfo.getFirstName()).isEqualTo("Max");
        assertThat(payerInfo.getLastName()).isEqualTo("Mustermann");
        assertThat(payerInfo.getEmail()).isEqualTo("max.mustermann@gmail.com");
        Address billingAddress = payerInfo.getBillingAddress();
        assertThat(billingAddress.getLine1()).isEqualTo("Kurfürstendamm 100");
        assertThat(billingAddress.getCity()).isEqualTo("Berlin");
        assertThat(billingAddress.getPostalCode()).isEqualTo("10709");
        assertThat(billingAddress.getCountryCode()).isEqualTo("DE");
    }
}
