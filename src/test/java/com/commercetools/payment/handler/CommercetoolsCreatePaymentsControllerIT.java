package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Nonnull;
import javax.money.MonetaryAmount;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.commercetools.helper.mapper.PaymentMapper.getApprovalUrl;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.createPaymentDraftBuilder;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getDummyComplexCartDraftWithDiscounts;
import static com.commercetools.util.CustomFieldUtil.getCustomFieldStringOrEmpty;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
// completely wipe-out CTP project Payment, Cart, Order endpoints before the test cases
public class CommercetoolsCreatePaymentsControllerIT {

    @Autowired
    private MockMvc mockMvc;

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
    public void finalSlashIsProcessedToo() throws Exception {
        this.mockMvc.perform(get("/asdhfasdfasf/commercetools/create/payments/6753324-23452-sgsfgd/"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldReturnNewPaypalPaymentId() throws Exception {
        final String paymentId = createCartAndPayment();
        MvcResult mvcResult = this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andReturn();

        // validate response json: statusCode and approvalUrl
        JsonNode responseBody = SphereJsonUtils.parse(mvcResult.getResponse().getContentAsString());

        String returnedApprovalUrl = responseBody.get(APPROVAL_URL).asText();
        assertThat(returnedApprovalUrl).isNotBlank();
        URL url = new URL(returnedApprovalUrl);
        assertThat(url.getProtocol()).isEqualTo("https");
        assertThat(url.getAuthority()).isEqualTo("www.sandbox.paypal.com");

        String pPPaymentToken = of(Pattern.compile("(?:^|&)token=([^&=?\\s]+)").matcher(url.getQuery()))
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .orElse(null);
        assertThat(pPPaymentToken).isNotBlank();

        // verify CTP payment values: approval url + interfaceId
        Payment updatedPayment = executeBlocking(ctpFacade.getPaymentService().getById(paymentId)).orElse(null);
        assertThat(updatedPayment).isNotNull();

        assertThat(getCustomFieldStringOrEmpty(updatedPayment, APPROVAL_URL)).isEqualTo(returnedApprovalUrl);

        String ppPaymentId = updatedPayment.getInterfaceId();
        assertThat(ppPaymentId).isNotNull();

        // try to fetch payment from PP and verify it
        // this line could be change if PaypalPlusPaymentService is extended to have "getById" functionality
        APIContextFactory apiContextFactory = tenantConfig.createAPIContextFactory();
        com.paypal.api.payments.Payment createdPpPayment =
                com.paypal.api.payments.Payment.get(apiContextFactory.createAPIContext(), ppPaymentId);

        assertThat(createdPpPayment).isNotNull();
        assertThat(createdPpPayment.getState()).isEqualTo("created");
        assertThat(createdPpPayment.getRedirectUrls().getCancelUrl()).startsWith("http://example.com/cancel/23456789");
        assertThat(createdPpPayment.getRedirectUrls().getReturnUrl()).startsWith("http://example.com/success/23456789");

        assertThat(getApprovalUrl(createdPpPayment)).contains(returnedApprovalUrl);
    }

    @Test
    public void whenPaymentIsMissing_shouldReturn4xxError() throws Exception {
        this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, "nonUUIDString")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, UUID.randomUUID().toString())))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenCartIsMissing_shouldReturn404() throws Exception {
        Payment payment = executeBlocking(createPaymentCompletationStage(Money.of(10, EUR), Locale.ENGLISH));
        MvcResult mvcResult = this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, payment.getId())))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();

        JsonNode responseBody = SphereJsonUtils.parse(mvcResult.getResponse().getContentAsString());
        assertThat(responseBody.get("errorMessage").asText()).isNotBlank();
    }

    @Test
    public void whenPaymentInterfaceIsIncorrect_shouldReturn400() throws Exception {
        PaymentDraftDsl dsl = createPaymentDraftBuilder(Money.of(10, EUR), Locale.ENGLISH)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface("NOT-PAYPAL-INTERFACE").method(PAYPAL).build())
                .build();

        Cart cart = executeBlocking(createCartCS()
                .thenCompose(c -> sphereClient.execute(PaymentCreateCommand.of(dsl))
                        .thenApply(payment -> new CtpPaymentWithCart(payment, c))
                        .thenCompose(ctpPaymentWithCart -> sphereClient.execute(CartUpdateCommand.of(ctpPaymentWithCart.getCart(),
                                AddPayment.of(ctpPaymentWithCart.getPayment()))))));

        String paymentId = cart.getPaymentInfo().getPayments().get(0).getId();
        MvcResult mvcResult = this.mockMvc.perform(post(format("/%s/commercetools/create/payments/%s", MAIN_TEST_TENANT_NAME, paymentId)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode responseBody = SphereJsonUtils.parse(mvcResult.getResponse().getContentAsString());
        assertThat(responseBody.get("errorMessage").asText()).isNotBlank();
    }

    private String createCartAndPayment() {
        Cart updatedCart = executeBlocking(createCartCS()
                .thenCompose(cart -> createPaymentCompletationStage(cart.getTotalPrice(), cart.getLocale())
                        .thenApply(payment -> new CtpPaymentWithCart(payment, cart))
                        .thenCompose(ctpPaymentWithCart -> sphereClient.execute(CartUpdateCommand.of(ctpPaymentWithCart.getCart(),
                                AddPayment.of(ctpPaymentWithCart.getPayment()))))));

        return updatedCart.getPaymentInfo().getPayments().get(0).getId();
    }

    private CompletionStage<Cart> createCartCS() {
        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
                .currency(EUR)
                .build();
        return sphereClient.execute(CartCreateCommand.of(dummyComplexCartWithDiscounts));
    }

    private CompletionStage<Payment> createPaymentCompletationStage(@Nonnull MonetaryAmount totalPrice,
                                                                    Locale locale) {
        PaymentDraftDsl dsl = createPaymentDraftBuilder(totalPrice, locale)
                .build();
        return sphereClient.execute(PaymentCreateCommand.of(dsl));
    }

}
