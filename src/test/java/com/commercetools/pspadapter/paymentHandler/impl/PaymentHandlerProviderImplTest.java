package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.Application;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.paypal.api.payments.Payment;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import io.sphere.sdk.types.CustomFieldsDraftBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getDummyComplexCartDraftWithDiscounts;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PaymentHandlerProviderImplTest {

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Autowired
    private PaymentHandlerProvider paymentHandlerProvider;

    @Autowired
    private PaymentMapper paymentMapper;

    private SphereClient sphereClient;

    private PaypalPlusFacade paypalPlusFacade;

    private CtpFacade ctpFacade;

    @Before
    public void setUp() {
        Optional<TenantConfig> tenantConfigOpt = tenantConfigFactory.getTenantConfig(MAIN_TEST_TENANT_NAME);
        sphereClient = tenantConfigOpt.map(TenantConfig::createSphereClient).orElse(null);

        this.paypalPlusFacade = tenantConfigOpt
                .map(tenantConfig -> new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade())
                .orElse(null);

        this.ctpFacade = tenantConfigOpt
                .map(tenantConfig -> new CtpFacadeFactory(tenantConfig).getCtpFacade())
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
    public void shouldPatchShippingAddress() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();
        paymentHandler.createPayment(ctpPaymentWithCart.getPayment().getId());

        Cart cartWithExpansion = executeBlocking(this.ctpFacade.getCartService().getByPaymentId(ctpPaymentWithCart.getPayment().getId(),
                "paymentInfo.payments[*]")).get();
        String paypalPlusPaymentId = cartWithExpansion.getPaymentInfo().getPayments().get(0).getObj().getInterfaceId();
        PaymentHandleResponse paymentHandleResult = paymentHandler.patchAddress(cartWithExpansion, paypalPlusPaymentId);
        assertThat(paymentHandleResult.getStatusCode()).isEqualTo(HttpStatus.OK.value());

        Payment pPPayment = this.paypalPlusFacade.getPaymentService().lookUp(paypalPlusPaymentId).toCompletableFuture().join();
        assertThat(pPPayment.getTransactions().get(0).getItemList().getShippingAddress()).isNotNull();
    }

    @Test
    public void shouldSetPayerId() {
        String testPayerId = "testPayerId";
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();

        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();
        paymentHandler.createPayment(ctpPaymentWithCart.getPayment().getId());
        io.sphere.sdk.payments.Payment ctpPayment = this.sphereClient.execute(PaymentByIdGet.of(ctpPaymentWithCart.getPayment().getId())).toCompletableFuture().join();

        paymentHandler.updatePayerIdInCtpPayment(ctpPayment.getInterfaceId(), testPayerId).toCompletableFuture().join();

        PaymentByIdGet paymentQuery = PaymentByIdGet.of(ctpPayment.getId());
        ctpPayment = sphereClient.execute(paymentQuery)
                .toCompletableFuture()
                .join();
        assertThat(ctpPayment.getCustom().getFieldAsString(PAYER_ID)).isEqualTo(testPayerId);
    }

    @Test
    public void shouldCreateChargeTransaction() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();

        Payment paypalPlusPayment = paymentMapper.ctpPaymentToPaypalPlus(ctpPaymentWithCart);
        paypalPlusPayment = executeBlocking(this.paypalPlusFacade.getPaymentService().create(paypalPlusPayment));

        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        io.sphere.sdk.payments.Payment chargeTransaction
                = paymentHandler.createChargeTransaction(paypalPlusPayment, ctpPaymentWithCart.getPayment().getId(), TransactionState.SUCCESS)
                .toCompletableFuture().join();

        assertThat(chargeTransaction).isNotNull();
    }

    @Test
    public void whenPaypalPaymentIsNotApproved_transactionShouldNotBeCreated() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        String ctpPaymentId = ctpPaymentWithCart.getPayment().getId();
        paymentHandler.createPayment(ctpPaymentId);

        io.sphere.sdk.payments.Payment ctpPayment = this.sphereClient
                .execute(PaymentByIdGet.of(ctpPaymentId)).toCompletableFuture().join();

        PaymentHandleResponse response = paymentHandler.executePayment(ctpPayment.getInterfaceId(), "invalidTestPayerId");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        ctpPayment = sphereClient.execute(PaymentByIdGet.of(ctpPaymentId)).toCompletableFuture().join();
        assertThat(ctpPayment.getTransactions()).isEmpty();
    }

    @Test
    public void whenOnPatchCartIsProvidedWithNoExpansion_500IsReturned() {
        CtpPaymentWithCart ctpPaymentWithCart = createCartWithPayment();
        PaymentHandler paymentHandler = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME).get();

        String ctpPaymentId = ctpPaymentWithCart.getPayment().getId();
        paymentHandler.createPayment(ctpPaymentId);

        io.sphere.sdk.payments.Payment ctpPayment = this.sphereClient
                .execute(PaymentByIdGet.of(ctpPaymentId)).toCompletableFuture().join();

        PaymentHandleResponse paymentHandleResponse = paymentHandler.patchAddress(ctpPaymentWithCart.getCart(),
                ctpPayment.getInterfaceId());

        assertThat(paymentHandleResponse.getStatusCode()).isEqualTo(500);
    }

    /**
     * Private methods
     **/

    private CtpPaymentWithCart createCartWithPayment() {
        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getDummyComplexCartDraftWithDiscounts())
                .currency(EUR)
                .build();

        CtpPaymentWithCart ctpPaymentWithCart = executeBlocking(sphereClient.execute(CartCreateCommand.of(dummyComplexCartWithDiscounts))
                .thenCompose(cart -> sphereClient.execute(PaymentCreateCommand.of(
                        PaymentDraftBuilder.of(cart.getTotalPrice())
                                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface(PAYPAL_PLUS).method(PAYPAL).build())
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