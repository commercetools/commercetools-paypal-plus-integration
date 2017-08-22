package com.commercetools.payment;

import com.commercetools.model.CtpPaymentWithCart;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;

import javax.annotation.Nonnull;
import javax.money.MonetaryAmount;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.createPaymentDraftBuilder;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getDummyComplexCartDraftWithDiscounts;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;

public class PaymentIntegrationTest {

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
        PaymentDraftDsl dsl = createPaymentDraftBuilder(totalPrice, locale)
                .build();
        return sphereClient.execute(PaymentCreateCommand.of(dsl));
    }
}