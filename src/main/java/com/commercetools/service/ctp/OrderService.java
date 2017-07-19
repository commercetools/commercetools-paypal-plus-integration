package com.commercetools.service.ctp;

import io.sphere.sdk.orders.Order;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * CTP service to work with {@link Order}s
 */
public interface OrderService {

    /**
     * Get an order which has one of {@code paymentInfo.payments.id = paymentId}.
     * <p>
     * This service does not expect we could have multiply orders for one payment
     *
     * @param paymentId CTP payment uuid
     * @return {@link Optional} {@link Order} if exists, empty {@link Optional} if not found.
     */
    CompletionStage<Optional<Order>> getByPaymentId(@Nullable String paymentId);
}
