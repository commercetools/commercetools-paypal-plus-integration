package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.Application;
import com.commercetools.config.bean.CtpConfigStartupValidator;
import com.commercetools.exception.IntegrationServiceException;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.PaymentMapperHelper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.ctp.CtpPaymentMethods;
import com.commercetools.pspadapter.facade.*;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.google.gson.Gson;
import com.paypal.api.payments.*;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import io.sphere.sdk.types.CustomFieldsDraftBuilder;
import org.bitbucket.radistao.test.annotation.AfterAllMethods;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.BasePaymentIT.getProductsInjectedCartDraft;
import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.ctp.ExpansionExpressions.PAYMENT_INFO_EXPANSION;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupAllTenantsTypes;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getDummyComplexCartDraftWithDiscounts;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static io.sphere.sdk.payments.TransactionState.SUCCESS;
import static io.sphere.sdk.payments.TransactionType.CHARGE;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class)
public class PaymentHandlerProviderImplIT {

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Autowired
    private PaymentHandlerProvider paymentHandlerProvider;

    @Autowired
    private PaymentMapperHelper paymentMapperHelper;

    @Autowired
    private PaypalPlusFacadeFactory paypalPlusFacadeFactory;

    @Autowired
    private CtpFacadeFactory ctpFacadeFactory;

    @Autowired
    protected SphereClientFactory sphereClientFactory;

    @Autowired
    @Qualifier("ctpConfigStartupValidatorImpl") // we need to test real CtpConfigStartupValidatorImpl here, not injected mock
    private CtpConfigStartupValidator ctpConfigStartupValidator;

    private SphereClient sphereClient;

    private PaypalPlusFacade paypalPlusFacade;

    private CtpFacade ctpFacade;

    @BeforeAllMethods
    public void setupBeforeAll() {
        ctpConfigStartupValidator.validateTypes();
    }

    /**
     * Cleanup CTP payment custom types crated in {@link #setupBeforeAll()}
     */
    @AfterAllMethods
    public void tearDownAfterAll() {
        cleanupAllTenantsTypes(tenantConfigFactory, sphereClientFactory);
    }

    @Before
    public void setUp() {
        Optional<TenantConfig> tenantConfigOpt = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME);
        sphereClient = tenantConfigOpt.map(sphereClientFactory::createSphereClient).orElse(null);

        this.paypalPlusFacade = tenantConfigOpt
                .map(tenantConfig -> paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig))
                .orElse(null);

        this.ctpFacade = tenantConfigOpt
                .map(tenantConfig -> ctpFacadeFactory.getCtpFacade(tenantConfig))
                .orElse(null);
    }

    @Test
    public void shouldReturnCorrectHandlerByTenantName() {
        Optional<PaymentHandler> paymentClientOpt = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME);
        assertThat(paymentClientOpt).isNotEmpty();
    }

    @Test
    public void whenTenantDoesNotExist_shouldReturnEmptyOptional() {
        Optional<PaymentHandler> paymentClientOpt = paymentHandlerProvider.getPaymentHandler("");
        assertThat(paymentClientOpt).isEmpty();
    }

    @Test
    @Ignore("Bug in Paypal Plus: https://github.com/paypal/PayPal-REST-API-issues/issues/124")
    public void shouldPatchShippingAddress() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();
        executeBlocking(paymentHandler.createPayment(ctpPaymentWithCart.getPayment().getId()));

        Cart cartWithExpansion = executeBlocking(this.ctpFacade.getCartService().getByPaymentId(ctpPaymentWithCart.getPayment().getId(),
                PAYMENT_INFO_EXPANSION)).get();
        String paypalPlusPaymentId = cartWithExpansion.getPaymentInfo().getPayments().get(0).getObj().getInterfaceId();
        PaymentHandleResponse paymentHandleResult = executeBlocking(paymentHandler.patchAddress(cartWithExpansion, paypalPlusPaymentId));
        assertThat(paymentHandleResult.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        Payment paypalPlusPayment = executeBlocking(this.paypalPlusFacade.getPaymentService().getByPaymentId(paypalPlusPaymentId));
        assertThat(paypalPlusPayment.getTransactions().get(0).getItemList().getShippingAddress()).isNotNull();
    }

    @Test
    public void shouldSetPayerId() {
        String testPayerId = "testPayerId";
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();

        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();
        executeBlocking(paymentHandler.createPayment(ctpPaymentWithCart.getPayment().getId()));
        io.sphere.sdk.payments.Payment ctpPayment = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentWithCart.getPayment().getId())));

        executeBlocking(paymentHandler.updatePayerIdInCtpPayment(ctpPayment.getInterfaceId(), testPayerId));

        PaymentByIdGet paymentQuery = PaymentByIdGet.of(ctpPayment.getId());
        ctpPayment = executeBlocking(sphereClient.execute(paymentQuery));
        assertThat(ctpPayment.getCustom().getFieldAsString(PAYER_ID)).isEqualTo(testPayerId);
    }

    @Test
    public void shouldCreateChargeTransaction() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();

        PaymentMapper paymentMapper = paymentMapperHelper.getPaymentMapper(ctpPaymentWithCart.getPaymentMethod())
                .orElse(null);
        assertThat(paymentMapper).isNotNull();

        Payment paypalPlusPayment = paymentMapper.ctpPaymentToPaypalPlus(ctpPaymentWithCart);
        paypalPlusPayment = executeBlocking(this.paypalPlusFacade.getPaymentService().create(paypalPlusPayment));

        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        // mock sale transaction in the execute response
        Amount amount = new Amount("EUR", "10.20");
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        RelatedResources relatedResources = new RelatedResources();
        relatedResources.setSale(new Sale("MOCK-SALE-ID", amount, "completed",
                paypalPlusPayment.getId(), paypalPlusPayment.getCreateTime()));
        transaction.setRelatedResources(singletonList(relatedResources));
        paypalPlusPayment.setTransactions(singletonList(transaction));

        io.sphere.sdk.payments.Payment updatedPayment = executeBlocking(paymentHandler
                .createChargeTransaction(paypalPlusPayment, ctpPaymentWithCart.getPayment().getId(), SUCCESS));

        assertThat(updatedPayment).isNotNull();
        assertThat(updatedPayment.getId()).isEqualTo(ctpPaymentWithCart.getPayment().getId());
        assertThat(updatedPayment.getTransactions()).hasSize(1);
        io.sphere.sdk.payments.Transaction chargeTxn = updatedPayment.getTransactions().get(0);
        assertThat(chargeTxn.getInteractionId()).isEqualTo("MOCK-SALE-ID");
        assertThat(chargeTxn.getType()).isEqualTo(CHARGE);
        assertThat(chargeTxn.getAmount()).isEqualTo(Money.of(10.20, EUR));
        assertThat(chargeTxn.getState()).isEqualTo(SUCCESS);
    }

    @Test
    public void whenPaypalPaymentIsNotApproved_transactionShouldNotBeCreated() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        String ctpPaymentId = ctpPaymentWithCart.getPayment().getId();
        executeBlocking(paymentHandler.createPayment(ctpPaymentId));

        io.sphere.sdk.payments.Payment ctpPayment = executeBlocking(this.sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));

        PaymentHandleResponse response = executeBlocking(paymentHandler.executePayment(ctpPayment.getInterfaceId(), "invalidTestPayerId"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ctpPayment = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));
        assertThat(ctpPayment.getTransactions()).isEmpty();
    }

    @Test
    public void whenOnPatchCartIsProvidedWithNoExpansion_500IsReturned() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        String ctpPaymentId = ctpPaymentWithCart.getPayment().getId();
        executeBlocking(paymentHandler.createPayment(ctpPaymentId));

        io.sphere.sdk.payments.Payment ctpPayment = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));

        PaymentHandleResponse paymentHandleResponse = executeBlocking(paymentHandler.patchAddress(ctpPaymentWithCart.getCart(),
                ctpPayment.getInterfaceId()));

        assertThat(paymentHandleResponse.getStatusCode()).isEqualTo(500);
    }

    @Test
    public void executePaymentPaypalPlus_withPaypalPlusServiceException() {
        // preparation
        final CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        final PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        final String ctpPaymentId = ctpPaymentWithCart.getPayment().getId();
        executeBlocking(paymentHandler.createPayment(ctpPaymentId));

        final io.sphere.sdk.payments.Payment ctpPayment = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));

        // test
        final PaymentHandleResponse paymentHandleResponse =
                executeBlocking(
                        paymentHandler.executePayment(ctpPayment.getInterfaceId(), ctpPayment.getInterfaceId()));

        // assertion
        assertThat(paymentHandleResponse.getStatusCode()).isEqualTo(400); //400 is thrown by Paypal Plus sandbox env
        final io.sphere.sdk.payments.Payment ctpPaymentAfterInteraction = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));
        assertThat(ctpPaymentAfterInteraction.getInterfaceInteractions()
                .stream().anyMatch(e->e.toString().contains("Value exceeds max length of 20")));
    }


    @Test
    public void executePaymentPaypalPlus_withIntegrationServiceException() {
        // preparation
        final CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        final String ctpPaymentId = ctpPaymentWithCart.getPayment().getId();

        final PaymentHandler paymentHandler = getPaymentHandlerProviderMock(ctpPaymentId)
                .getPaymentHandler(MAIN_TEST_TENANT_NAME)
                .get();
        executeBlocking(paymentHandler.createPayment(ctpPaymentId));

        final io.sphere.sdk.payments.Payment ctpPayment = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));

        // test
        final PaymentHandleResponse paymentHandleResponse =
                executeBlocking(paymentHandler.executePayment(ctpPayment.getInterfaceId(), "test"));

        // assertion
        assertThat(paymentHandleResponse.getStatusCode()).isEqualTo(500);
        final io.sphere.sdk.payments.Payment ctpPaymentAfterInteraction = executeBlocking(sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)));
        assertThat(ctpPaymentAfterInteraction.getInterfaceInteractions()
                .stream().anyMatch(e->e.toString().contains("com.commercetools.exception.IntegrationServiceException")));
    }


    @Nonnull
    private PaymentHandlerProvider getPaymentHandlerProviderMock(String ctpPaymentId) {
        return new PaymentHandlerProviderImpl(
                tenantConfigFactory,
                new PaypalPlusFacadeFactoryMock(ctpPaymentId),
                paymentMapperHelper,
                new Gson(),
                new PaypalPlusFormatterImpl(),
                ctpFacadeFactory);
    }

    /**
     * Private class to mock PaypalPlus behavior only for executePaymentPaypalPlus_withIntegrationServiceException
     */
    private class PaypalPlusFacadeFactoryMock extends PaypalPlusFacadeFactory{
        private String ctpPaymentId;

        public PaypalPlusFacadeFactoryMock(String ctpPaymentId) {
            this.ctpPaymentId = ctpPaymentId;
        }

        @Override
        public PaypalPlusFacade getPaypalPlusFacade(TenantConfig tenantConfig) {
            final PaypalPlusFacade paypalPlusFacade = mock(PaypalPlusFacade.class);
            final PaypalPlusPaymentService paymentService = getPaypalPlusPaymentServiceMock(this.ctpPaymentId);
            when(paypalPlusFacade.getPaymentService()).thenReturn(paymentService);

            return paypalPlusFacade;
        }
    }

    @Nonnull
    private PaypalPlusPaymentService getPaypalPlusPaymentServiceMock(String ctpPaymentId) {
        final PaypalPlusPaymentService paymentService = mock(PaypalPlusPaymentService.class);

        Throwable integrationServiceException = new IntegrationServiceException("Paypal Plus REST service unexpected exception.");
        when(paymentService.execute(any(Payment.class), any(PaymentExecution.class)))
                .thenThrow(new IntegrationServiceException("testing message", integrationServiceException));

        Payment testPayment = new Payment();
        testPayment.setId(ctpPaymentId);
        when(paymentService.create(any(Payment.class)))
                .thenReturn(CompletableFuture.completedFuture(testPayment));
        return paymentService;
    }

    /**
     * Private methods
     **/
    private CtpPaymentWithCart createCartWithPayment() {
        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
                .currency(EUR)
                .build();

        CartDraft cartDraft = getProductsInjectedCartDraft(sphereClient, dummyComplexCartWithDiscounts);

        CtpPaymentWithCart ctpPaymentWithCart = executeBlocking(sphereClient.execute(CartCreateCommand.of(cartDraft))
                .thenCompose(cart -> sphereClient.execute(PaymentCreateCommand.of(
                        PaymentDraftBuilder.of(cart.getTotalPrice())
                                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface(PAYPAL_PLUS).method(CtpPaymentMethods.DEFAULT).build())
                                .custom(CustomFieldsDraftBuilder.ofTypeKey("payment-paypal")
                                        .addObject(SUCCESS_URL_FIELD, "http://example.com/success/23456789")
                                        .addObject(CANCEL_URL_FIELD, "http://example.com/cancel/23456789")
                                        .addObject(REFERENCE, "23456789")
                                        .addObject(LANGUAGE_CODE_FIELD, ofNullable(cart.getLocale()).orElse(DEFAULT_LOCALE).getLanguage())
                                        .build())
                                .build()))
                        .thenCompose(payment -> sphereClient.execute(CartUpdateCommand.of(cart, AddPayment.of(payment)))
                                .thenApply(c -> new CtpPaymentWithCart(payment, c))
                        )));

        return ctpPaymentWithCart;
    }

}