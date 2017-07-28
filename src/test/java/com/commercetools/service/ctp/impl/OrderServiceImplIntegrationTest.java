package com.commercetools.service.ctp.impl;

import com.commercetools.Application;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.testUtil.ctpUtil.CtpResourcesUtil;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import com.commercetools.testUtil.customTestConfigs.TaxSetupConfig;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.orders.Order;
import io.sphere.sdk.orders.OrderFromCartDraft;
import io.sphere.sdk.orders.commands.OrderFromCartCreateCommand;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.queries.TaxCategoryQuery;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.customTestConfigs.TaxSetupConfig.TAX_CATEGORY_NAME;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static org.assertj.core.api.Assertions.assertThat;

// TODO: move to separate integration test
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Import(value = {
        OrdersCartsPaymentsCleanupConfiguration.class,
        TaxSetupConfig.class
})
public class OrderServiceImplIntegrationTest {

    @Autowired
    private SphereClient sphereClient;

    @Autowired
    private OrderService orderService;

    @Test
    public void createOrderManuallyAndGetByPaymentId() {
        PaymentDraftDsl paymentDraft = PaymentDraftBuilder.of(Money.of(22.33, EUR))
                .build();
        Payment ctPayment = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(paymentDraft)));

        createOrderWithPayment(ctPayment);

        Optional<Order> orderOpt = executeBlocking(orderService.getByPaymentId(ctPayment.getId()));
        assertThat(orderOpt).isNotEmpty();
        String paymentId = orderOpt.get().getPaymentInfo().getPayments().get(0).getId();
        assertThat(paymentId).isEqualTo(ctPayment.getId());
    }

    @Test
    public void getWithWrongPaymentId() {
        Optional<Order> orderOpt1 = executeBlocking(orderService.getByPaymentId(null));
        assertThat(orderOpt1).isEmpty();

        Optional<Order> orderOpt2 = executeBlocking(orderService.getByPaymentId(""));
        assertThat(orderOpt2).isEmpty();
    }

    private void createOrderWithPayment(Payment ctPayment) {
        TaxCategory taxCategory = executeBlocking(sphereClient.execute(
                TaxCategoryQuery.of().plusPredicates(m -> m.name().is(TAX_CATEGORY_NAME)))
        ).head().get();

        CartDraft cartDraft = CtpResourcesUtil.getCartDraftWithCustomLineItems(taxCategory);

        Cart ctCart = executeBlocking(sphereClient.execute(CartCreateCommand.of(cartDraft)));

        Cart ctCartWithpayment = executeBlocking(sphereClient.execute(CartUpdateCommand.of(ctCart, AddPayment.of(ctPayment))));

        OrderFromCartDraft orderFromCartDraft = OrderFromCartDraft.of(ctCartWithpayment);
        executeBlocking(sphereClient.execute(OrderFromCartCreateCommand.of(orderFromCartDraft)));
    }
}