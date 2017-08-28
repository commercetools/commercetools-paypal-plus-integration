package com.commercetools.service.ctp.impl;

import com.commercetools.service.ctp.CartService;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.queries.CartQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.expansion.ExpansionPath;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.queries.PaymentQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import io.sphere.sdk.queries.PagedResult;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class CartServiceImpl extends BaseSphereService implements CartService {

    public CartServiceImpl(@Nonnull SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Optional<Cart>> getByPaymentId(@Nullable String ctpPaymentId) {
        return getByPaymentId(ctpPaymentId, null);
    }

    @Override
    public CompletionStage<Optional<Cart>> getByPaymentId(@Nullable String ctpPaymentId,
                                                          @Nullable String expansionPath) {
        if (StringUtils.isEmpty(ctpPaymentId)) {
            return completedFuture(Optional.empty());
        }
        CartQuery cartQuery = CartQuery.of()
                .withPredicates(m -> m.paymentInfo().payments().id().is(ctpPaymentId));
        if (StringUtils.isNotBlank(expansionPath)) {
            cartQuery = cartQuery.plusExpansionPaths(
                    Collections.singletonList(ExpansionPath.of(expansionPath))
            );
        }
        return sphereClient.execute(cartQuery).thenApplyAsync(PagedQueryResult::head);
    }

    @Override
    public CompletionStage<Optional<Cart>> getByPaymentMethodAndInterfaceId(@Nullable String paymentMethodInterface,
                                                                            @Nullable String interfaceId) {
        return getByPaymentMethodAndInterfaceId(paymentMethodInterface, interfaceId);
    }

    @Override
    public CompletionStage<Optional<Cart>> getByPaymentMethodAndInterfaceId(@Nullable String paymentMethodInterface,
                                                                            @Nullable String interfaceId,
                                                                            @Nullable String expansionPath) {
        if (isBlank(paymentMethodInterface) || isBlank(interfaceId)) {
            return completedFuture(null);
        }
        PaymentQuery paymentQuery = PaymentQuery.of()
                .withPredicates(p -> p.paymentMethodInfo().paymentInterface().is(paymentMethodInterface))
                .plusPredicates(p -> p.interfaceId().is(interfaceId));
        return sphereClient.execute(paymentQuery)
                .thenApply(PagedResult::head)
                .thenCompose(optPayment -> getByPaymentId(optPayment.map(Payment::getId).orElse(null), expansionPath));
    }
}