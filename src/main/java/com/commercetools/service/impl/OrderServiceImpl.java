package com.commercetools.service.impl;

import com.commercetools.service.OrderService;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.orders.Order;
import io.sphere.sdk.orders.queries.OrderQuery;
import io.sphere.sdk.orders.queries.OrderQueryBuilder;
import io.sphere.sdk.queries.PagedQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

@Service
public class OrderServiceImpl extends BaseSphereService implements OrderService {

    @Autowired
    public OrderServiceImpl(SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Optional<Order>> getByPaymentId(String paymentId) {
        OrderQuery orderWithPaymentId = OrderQueryBuilder.of()
                .predicates(order -> order.paymentInfo().payments().id().is(paymentId)).build();
        return sphereClient.execute(orderWithPaymentId).thenApplyAsync(PagedQueryResult::head);
    }
    
}
