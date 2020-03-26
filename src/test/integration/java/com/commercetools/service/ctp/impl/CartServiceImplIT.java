package com.commercetools.service.ctp.impl;

import com.commercetools.Application;
import com.commercetools.service.ctp.CartService;
import com.commercetools.testUtil.ctpUtil.CtpResourcesUtil;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.queries.TaxCategoryQuery;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupOrdersCartsPayments;
import static com.commercetools.testUtil.ctpUtil.TaxUtil.TAX_CATEGORY_NAME;
import static com.commercetools.testUtil.ctpUtil.TaxUtil.ensureTestTaxCategory;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class,properties = {"spring.main.allow-bean-definition-overriding=true"})
public class CartServiceImplIT {

    @Autowired
    private SphereClient sphereClient;

    @Autowired
    private CartService cartService;

    @BeforeAllMethods
    public void setupBeforeAll() {
        cleanupOrdersCartsPayments(sphereClient);
    }

    @Test
    public void createCartManuallyAndGetByPaymentId() {
        PaymentDraftDsl paymentDraft = PaymentDraftBuilder.of(Money.of(22.33, EUR))
                .build();
        Payment ctPayment = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(paymentDraft)));

        TaxCategory taxCategory = ensureTestTaxCategory(sphereClient);

        CartDraft cartDraft = CtpResourcesUtil.getCartDraftWithCustomLineItems(taxCategory);

        Cart ctCart = executeBlocking(sphereClient.execute(CartCreateCommand.of(cartDraft)));

        executeBlocking(sphereClient.execute(CartUpdateCommand.of(ctCart, AddPayment.of(ctPayment))));

        Optional<Cart> cartOpt = executeBlocking(cartService.getByPaymentId(ctPayment.getId()));

        assertThat(cartOpt).isNotEmpty();
    }

    @Test
    public void getWithWrongPaymentId() {
        Optional<Cart> cartOpt1 = executeBlocking(cartService.getByPaymentId(null));
        assertThat(cartOpt1).isEmpty();

        Optional<Cart> cartOpt2 = executeBlocking(cartService.getByPaymentId(""));
        assertThat(cartOpt2).isEmpty();
    }
}
