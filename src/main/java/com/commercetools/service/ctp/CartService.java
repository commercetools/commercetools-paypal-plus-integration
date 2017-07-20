package com.commercetools.service.ctp;

import io.sphere.sdk.carts.Cart;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * CTP service to work with {@link Cart}s
 */
public interface CartService {

    CompletionStage<Optional<Cart>> getByPaymentId(@Nullable String paymentId);
}