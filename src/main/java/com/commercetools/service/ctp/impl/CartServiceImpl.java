package com.commercetools.service.ctp.impl;

import com.commercetools.service.ctp.CartService;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.queries.CartQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.queries.PagedQueryResult;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CartServiceImpl extends BaseSphereService implements CartService {

    public CartServiceImpl(SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Optional<Cart>> getByPaymentId(@Nullable String paymentId) {
        if (StringUtils.isEmpty(paymentId)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        CartQuery cartQuery = CartQuery.of().withPredicates(m -> m.paymentInfo().payments().id().is(paymentId));
        return sphereClient.execute(cartQuery).thenApplyAsync(PagedQueryResult::head);
    }
}