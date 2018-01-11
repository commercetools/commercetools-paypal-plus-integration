package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.PaymentIntegrationTest;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.SphereClientFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.test.web.servlet.MockMvcAsync;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.types.CustomFieldsDraftBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.util.CustomFieldUtil.getCustomFieldStringOrEmpty;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;
import static javax.swing.Action.DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test payment creation with application_context#shipping_preference
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
// 1) completely wipe-out CTP project Payment, Cart, Order endpoints before the test cases
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
public class CommercetoolsCreatePaymentWithShippingPreferenceIT extends PaymentIntegrationTest {

    @Autowired
    private MockMvcAsync mockMvcAsync;

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Autowired
    private CtpFacadeFactory ctpFacadeFactory;

    @Autowired
    private SphereClientFactory sphereClientFactory;

    private TenantConfig tenantConfig;
    private SphereClient sphereClient;
    private CtpFacade ctpFacade;

    @Before
    public void setUp() throws Exception {
        tenantConfig = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME)
                .orElseThrow(IllegalStateException::new);
        ctpFacade = ctpFacadeFactory.getCtpFacade(tenantConfig);
        sphereClient = sphereClientFactory.createSphereClient(tenantConfig);
    }

    @Test
    public void paymentWithShippingPreferenceCreated() throws Exception {
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

        // patch shipping address since it is required to have shipping address defined to be unchangeable on the approval page
        mockMvcAsync.performAsync(post(format("/%s/commercetools/patch/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

        com.paypal.api.payments.Payment createdPpPayment = getPpPayment(tenantConfig, ppPaymentId);

        assertCustomFields(createdPpPayment, returnedApprovalUrl, ppPaymentId);

        // jury rigg for Payment instance: Paypal Plus SDK's Payment returns stripped Payment version
        // without application context, see https://github.com/paypal/PayPal-Java-SDK/issues/330
        // As soon as this issue fixed - this assert will fail, but the next one should be activated then
        assertThat(createdPpPayment.toJSON().toLowerCase())
                .withFailMessage("If this test fails, this means application context issue is resolved "
                        + "(https://github.com/paypal/PayPal-Java-SDK/issues/330), "
                        + "e.g. application context with shipping preference is implemented, "
                        + "thus the assert below should be uncommented")
                .doesNotContainPattern(Pattern.compile("application.?context", CASE_INSENSITIVE | MULTILINE))
                .doesNotContainPattern(Pattern.compile("shipping.?preference", CASE_INSENSITIVE | MULTILINE));

        //assertThat(createdPpPayment.getApplicationContext()).isEqualTo(new ApplicationContext().setShippingPreference("SET_PROVIDED_ADDRESS"));
    }

    @Override
    protected PaymentDraftBuilder createPaymentDraftBuilder(@Nonnull MonetaryAmount totalPrice, @Nullable Locale locale) {
        return super.createPaymentDraftBuilder(totalPrice, locale)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of()
                        .paymentInterface(PAYPAL_PLUS)
                        .method(DEFAULT)
                        .build())
                .custom(CustomFieldsDraftBuilder.ofTypeKey("payment-paypal")
                        .addObject(SUCCESS_URL_FIELD, "http://example.com/success/23456789")
                        .addObject(CANCEL_URL_FIELD, "http://example.com/cancel/23456789")
                        .addObject(REFERENCE, "556677889900")
                        .addObject(LANGUAGE_CODE_FIELD, ofNullable(locale).orElse(DEFAULT_LOCALE).getLanguage())

                        // exactly this field is validated in current test
                        .addObject(SHIPPING_PREFERENCE, "SET_PROVIDED_ADDRESS")
                        .build());
    }
}
