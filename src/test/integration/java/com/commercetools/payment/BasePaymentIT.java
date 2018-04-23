package com.commercetools.payment;

import com.commercetools.config.bean.CtpConfigStartupValidator;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.SphereClientFactory;
import com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.test.web.servlet.MockMvcAsync;
import com.commercetools.testUtil.ctpUtil.CtpResourcesUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.paypal.base.rest.PayPalRESTException;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.expansion.ExpansionPath;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import io.sphere.sdk.types.CustomFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.commercetools.helper.mapper.PaymentMapper.getApprovalUrl;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.TIMESTAMP_FIELD;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupAllTenantsTypes;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupOrdersCartsPaymentsTypes;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getDummyComplexCartDraftWithDiscounts;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

public class BasePaymentIT {

    @Autowired
    protected MockMvcAsync mockMvcAsync;

    @Autowired
    protected TenantConfigFactory tenantConfigFactory;

    @Autowired
    protected CtpFacadeFactory ctpFacadeFactory;

    @Autowired
    protected SphereClientFactory sphereClientFactory;

    @Autowired
    @Qualifier("ctpConfigStartupValidatorImpl") // for payment tests real types must be created, thus real validator is injected
    protected CtpConfigStartupValidator ctpConfigStartupValidator;

    protected TenantConfig tenantConfig;
    protected SphereClient sphereClient;
    protected CtpFacade ctpFacade;

    /**
     * <ol>
     * <li>Cleanup orders, carts and payments storages.</li>
     * <li>Create CTP payment custom types</li>
     * </ol>
     */
    public void setupBeforeAll() {
        initTenantConfigs();
        cleanupOrdersCartsPaymentsTypes(sphereClient);
        ctpConfigStartupValidator.validateTypes();
    }

    /**
     * Cleanup CTP payment custom types crated in {@link #setupBeforeAll()}
     */
    public void tearDownAfterAll() {
        cleanupAllTenantsTypes(tenantConfigFactory, sphereClientFactory);
    }

    public void setUp() {
        initTenantConfigs();
    }

    /**
     * Instantiate request/tenant specific values (configs, facades, factories, clients) before each test.
     */
    protected void initTenantConfigs() {
        tenantConfig = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME)
                .orElseThrow(IllegalStateException::new);
        ctpFacade = ctpFacadeFactory.getCtpFacade(tenantConfig);
        sphereClient = sphereClientFactory.createSphereClient(tenantConfig);
    }

    protected String createCartAndPayment(@Nonnull SphereClient sphereClient) {
        Cart updatedCart = executeBlocking(createCartCS(sphereClient)
                .thenCompose(cart -> createPaymentCS(sphereClient, cart.getTotalPrice(), cart.getLocale())
                        .thenApply(payment -> new CtpPaymentWithCart(payment, cart))
                        .thenCompose(ctpPaymentWithCart -> sphereClient.execute(CartUpdateCommand.of(ctpPaymentWithCart.getCart(),
                                AddPayment.of(ctpPaymentWithCart.getPayment()))))));

        return updatedCart.getPaymentInfo().getPayments().get(0).getId();
    }

    protected CompletionStage<Cart> createCartCS(@Nonnull SphereClient sphereClient) {
        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
                .currency(EUR)
                .build();
        return sphereClient.execute(CartCreateCommand.of(dummyComplexCartWithDiscounts));
    }

    protected CompletionStage<Payment> createPaymentCS(@Nonnull SphereClient sphereClient,
                                                       @Nonnull MonetaryAmount totalPrice,
                                                       @Nonnull Locale locale) {
        PaymentDraftDsl dsl = createPaymentDraftBuilder(totalPrice, locale).build();
        return sphereClient.execute(PaymentCreateCommand.of(dsl));
    }

    protected PaymentDraftBuilder createPaymentDraftBuilder(@Nonnull MonetaryAmount totalPrice,
                                                            @Nullable Locale locale) {
        return CtpResourcesUtil.createPaymentDraftBuilder(totalPrice, locale);
    }

    protected static String verifyApprovalUrl(MvcResult mvcResult) throws IOException {
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

        return returnedApprovalUrl;
    }

    protected static void assertInterfaceInteractions(String paymentId, SphereClient sphereClient) {
        Payment paymentWithExpandedInteractions = executeBlocking(sphereClient.execute(
                PaymentByIdGet.of(paymentId).plusExpansionPaths(ExpansionPath.of("interfaceInteractions[*].type"))
        ));

        assertThat(paymentWithExpandedInteractions.getInterfaceInteractions()).hasSize(2);
        Optional<CustomFields> requestInteractionOpt = paymentWithExpandedInteractions.getInterfaceInteractions().stream()
                .filter(customFields -> customFields.getType().getObj().getKey().equals(InterfaceInteractionType.REQUEST.getInterfaceKey()))
                .findAny();

        assertThat(requestInteractionOpt).isNotEmpty();
        assertThat(requestInteractionOpt.get().getFieldAsString(TIMESTAMP_FIELD)).isNotEmpty();
        String request = requestInteractionOpt.get().getFieldAsString(
                InterfaceInteractionType.REQUEST.getValueFieldName()
        );
        JsonNode requestJson = SphereJsonUtils.parse(request);
        assertThat(requestJson.get("redirect_urls")).isNotNull();

        Optional<CustomFields> responseInteractionOpt = paymentWithExpandedInteractions.getInterfaceInteractions().stream()
                .filter(customFields -> customFields.getType().getObj().getKey().equals(InterfaceInteractionType.RESPONSE.getInterfaceKey()))
                .findAny();

        assertThat(responseInteractionOpt).isNotEmpty();
        assertThat(responseInteractionOpt.get().getFieldAsString(TIMESTAMP_FIELD)).isNotEmpty();
        String response = responseInteractionOpt.get().getFieldAsString(
                InterfaceInteractionType.RESPONSE.getValueFieldName()
        );
        assertThat(response).isNotEmpty();
        assertThat(SphereJsonUtils.parse(response)).isNotEmpty();
    }

    protected static com.paypal.api.payments.Payment getPpPayment(TenantConfig tenantConfig, String ppPaymentId) throws PayPalRESTException {
        APIContextFactory apiContextFactory = tenantConfig.getAPIContextFactory();
        return com.paypal.api.payments.Payment.get(apiContextFactory.createAPIContext(), ppPaymentId);
    }

    protected static void assertCustomFields(com.paypal.api.payments.Payment createdPpPayment, String returnedApprovalUrl, String ppPaymentId) throws PayPalRESTException {
        // try to fetch payment from PP and verify it
        // this line could be change if PaypalPlusPaymentService is extended to have "getById" functionality
        assertThat(createdPpPayment).isNotNull();
        assertThat(createdPpPayment.getState()).isEqualTo("created");
        assertThat(createdPpPayment.getRedirectUrls().getCancelUrl()).startsWith("http://example.com/cancel/23456789");
        assertThat(createdPpPayment.getRedirectUrls().getReturnUrl()).startsWith("http://example.com/success/23456789");

        assertThat(getApprovalUrl(createdPpPayment)).contains(returnedApprovalUrl);
    }
}