package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.payment.BasePaymentIT;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.SetBillingAddress;
import io.sphere.sdk.payments.Payment;
import org.bitbucket.radistao.test.annotation.AfterAllMethods;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.javamoney.moneta.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Locale;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static io.sphere.sdk.models.DefaultCurrencyUnits.USD;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsPatchPaymentsControllerIT extends BasePaymentIT {

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

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void finalSlashIsProcessedToo() throws Exception {
        final String ctpPaymentId = createCartAndPayment(sphereClient);
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)));

        mockMvcAsync.performAsync(post(format("/%s/commercetools/patch/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andExpect(status().isOk());

        String anotherCtpPaymentId = createCartAndPayment(sphereClient);
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s/",
                MAIN_TEST_TENANT_NAME, anotherCtpPaymentId)));

        mockMvcAsync.performAsync(post(format("/%s/commercetools/patch/payments/%s/",
                MAIN_TEST_TENANT_NAME, anotherCtpPaymentId)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldPatchPaypalPaymentWithBillingAndShippingAddresses() throws Exception {
        final String ctpPaymentId = createCartAndPayment(sphereClient);
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)));

        mockMvcAsync.performAsync(post(format("/%s/commercetools/patch/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andExpect(status().isOk());
        Payment ctpPayment = executeBlocking(ctpFacade.getPaymentService().getById(ctpPaymentId)).get();
        assertThat(ctpPayment.getInterfaceInteractions().size()).isEqualTo(4);

        // A bug from Paypal makes this test sometimes fails, sometimes not
        // until it's fixed, it will be disabled
        // https://github.com/paypal/PayPal-REST-API-issues/issues/124
//        com.paypal.api.payments.Payment ppPayment = executeBlocking(ppFacade.getPaymentService().getByPaymentId(ctpPayment.getInterfaceId()));
//        assertThat(ppPayment.getTransactions().get(0).getItemList().getShippingAddress()).isNotNull();
//        assertThat(ppPayment.getPayer().getPayerInfo().getBillingAddress()).isNotNull();
    }

    @Test
    public void whenBillingAddressIsNull_shouldReturnPaymentWithShippingAddress() throws Exception {
        final String ctpPaymentId = createCartAndPayment(sphereClient);
        Cart cart = executeBlocking(ctpFacade.getCartService().getByPaymentId(ctpPaymentId)).get();
        executeBlocking(sphereClient.execute(CartUpdateCommand.of(cart, SetBillingAddress.of(null))));
        mockMvcAsync.performAsync(post(format("/%s/commercetools/create/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)));

        mockMvcAsync.performAsync(post(format("/%s/commercetools/patch/payments/%s",
                MAIN_TEST_TENANT_NAME, ctpPaymentId)))
                .andExpect(status().isOk());
//        Payment ctpPayment = executeBlocking(ctpFacade.getPaymentService().getById(ctpPaymentId)).get();

        // A bug from Paypal makes this test sometimes fails, sometimes not
        // until it's fixed, it will be disabled
        // https://github.com/paypal/PayPal-REST-API-issues/issues/124
//        com.paypal.api.payments.Payment ppPayment = executeBlocking(ppFacade.getPaymentService()
//                .getByPaymentId(ctpPayment.getInterfaceId()));
//        assertThat(ppPayment.getTransactions().get(0).getItemList().getShippingAddress()).isNotNull();
//        assertThat(ppPayment.getPayer().getPayerInfo().getBillingAddress()).isNotNull();
    }

    @Test
    public void whenPaymentHasNoCart_shouldThrow400Error() throws Exception {
        Payment payment = executeBlocking(createPaymentCS(sphereClient, Money.of(10, USD), Locale.ENGLISH));
        mockMvcAsync.performAsync(post(format("/%s/commercetools/patch/payments/%s",
                MAIN_TEST_TENANT_NAME, payment.getId())))
                .andExpect(status().isBadRequest());
    }


}