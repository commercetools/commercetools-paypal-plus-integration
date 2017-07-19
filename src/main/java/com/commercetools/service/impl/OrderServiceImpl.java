package com.commercetools.service.impl;

import com.commercetools.service.OrderService;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.orders.Order;
import io.sphere.sdk.orders.queries.OrderQuery;
import io.sphere.sdk.orders.queries.OrderQueryBuilder;
import io.sphere.sdk.queries.PagedQueryResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Service
public class OrderServiceImpl extends BaseSphereService implements OrderService {

    @Autowired
    public OrderServiceImpl(@Nonnull SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Optional<Order>> getByPaymentId(@Nullable String paymentId) {
        if (StringUtils.isEmpty(paymentId)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        OrderQuery orderWithPaymentId = OrderQueryBuilder.of()
                .predicates(order -> order.paymentInfo().payments().id().is(paymentId)).build();
        return sphereClient.execute(orderWithPaymentId).thenApplyAsync(PagedQueryResult::head);
    }
    
}
