package com.commercetools.payment;

import com.commercetools.config.bean.CtpConfigStartupValidator;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.pspadapter.ExtendedAPIContextFactory;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.SphereClientFactory;
import com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.test.web.servlet.MockMvcAsync;
import com.commercetools.testUtil.ctpUtil.CtpResourcesUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.neovisionaries.i18n.CountryCode;
import com.paypal.base.rest.PayPalRESTException;
import io.sphere.sdk.carts.*;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.expansion.ExpansionPath;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.models.LocalizedEnumValue;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import io.sphere.sdk.products.*;
import io.sphere.sdk.products.attributes.AttributeDefinitionDraft;
import io.sphere.sdk.products.attributes.AttributeDefinitionDraftBuilder;
import io.sphere.sdk.products.attributes.AttributeDefinitionDraftDsl;
import io.sphere.sdk.products.attributes.AttributeDraft;
import io.sphere.sdk.products.attributes.LocalizedEnumAttributeType;
import io.sphere.sdk.products.commands.ProductCreateCommand;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.ProductTypeDraftBuilder;
import io.sphere.sdk.producttypes.ProductTypeDraftDsl;
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.TaxCategoryDraftBuilder;
import io.sphere.sdk.taxcategories.TaxCategoryDraftDsl;
import io.sphere.sdk.taxcategories.TaxRateDraft;
import io.sphere.sdk.taxcategories.commands.TaxCategoryCreateCommand;
import io.sphere.sdk.types.CustomFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.commercetools.helper.mapper.PaymentMapper.getApprovalUrl;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.TIMESTAMP_FIELD;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.*;
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
        cleanupProductsProductTypesTaxCategories(sphereClient);
    }

    public void tearDown() {
        cleanupProductsProductTypesTaxCategories(sphereClient);
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
                .locale(Locale.GERMANY)
                .build();
        CartDraft cartDraft = getProductsInjectedCartDraft(sphereClient, dummyComplexCartWithDiscounts);

        return sphereClient.execute(CartCreateCommand.of(cartDraft));
    }

    public static CartDraft getProductsInjectedCartDraft(@Nonnull SphereClient sphereClient, CartDraft dummyComplexCartWithDiscounts) {
        //Product Type attribute
        AttributeDefinitionDraftDsl attributeDefinitionDraftDsl = AttributeDefinitionDraftBuilder.of(LocalizedEnumAttributeType.of(
                LocalizedEnumValue.of("TestKey", LocalizedString.ofEnglish("TestAttributeValue"))
        ), "marke", LocalizedString.ofEnglish("marke"), false).build();
        List<AttributeDefinitionDraft> attributeDefinitionDraftList = Stream.of(attributeDefinitionDraftDsl).collect(Collectors.toList());

        //Create the product
        ProductTypeDraftDsl typeDraftDsl = ProductTypeDraftBuilder.of(UUID.randomUUID().toString(), "testProd01", "testProd01", attributeDefinitionDraftList).build();
        ProductTypeCreateCommand productTypeCreateCommand = ProductTypeCreateCommand.of(typeDraftDsl);
        CompletionStage<Product> productCompletionStage = sphereClient.execute(productTypeCreateCommand)
                .thenCompose(productType -> createDummyProduct(sphereClient, productType));

        //Inject to the CartDraft
        return injectDummyProductsTaxCategory(productCompletionStage, dummyComplexCartWithDiscounts);
    }

    private static CompletionStage<Product> createDummyProduct(@Nonnull SphereClient sphereClient, ProductType productType){
        //Create the tax category
        TaxRateDraft taxRateDraft = TaxRateDraft.of("sample", 0.5, true, CountryCode.DE);
        TaxCategoryDraftDsl test_taxCategory = TaxCategoryDraftBuilder.of(UUID.randomUUID().toString(), Collections.emptyList(), null)
                .taxRates(Arrays.asList(taxRateDraft)).build();
        CompletionStage<TaxCategory> taxCategoryCompletionStage = sphereClient.execute(TaxCategoryCreateCommand.of(test_taxCategory));
        TaxCategory taxCategory = taxCategoryCompletionStage.toCompletableFuture().join();

        ProductVariantDraftDsl variantDraftDsl = ProductVariantDraftBuilder.of().price(PriceDraft.of(BigDecimal.valueOf(100), EUR))
                .attributes(AttributeDraft.of("marke",
                        LocalizedEnumValue.of("TestKey", LocalizedString.ofEnglish("TestAttributeValue"))))
                .build();

        ProductDraftDsl productDraftDsl = ProductDraftBuilder
                .of(productType, LocalizedString.ofEnglish("TestProd1"), LocalizedString.ofEnglish(UUID.randomUUID().toString()), Collections.emptyList())
                .taxCategory(taxCategory).masterVariant(variantDraftDsl).publish(false).build();
        return sphereClient.execute(ProductCreateCommand.of(productDraftDsl));
    }

    private static CartDraft injectDummyProductsTaxCategory(CompletionStage<Product> productCompletionStage,
                                                            CartDraft dummyComplexCartWithDiscounts){

        Product product = productCompletionStage.toCompletableFuture().join();


        LineItemDraftDsl lineItemDraftDsl = LineItemDraft.of(product, 1, 2);
        return CartDraftBuilder.of(EUR).plusLineItems(lineItemDraftDsl).shippingMethod(dummyComplexCartWithDiscounts.getShippingMethod())
                .shippingAddress(dummyComplexCartWithDiscounts.getShippingAddress()).taxMode(dummyComplexCartWithDiscounts.getTaxMode())
                .customerId(dummyComplexCartWithDiscounts.getCustomerId()).build();
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
        ExtendedAPIContextFactory extendedApiContextFactory = tenantConfig.getAPIContextFactory();
        return com.paypal.api.payments.Payment.get(extendedApiContextFactory.createAPIContext().getApiContext(), ppPaymentId);
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
