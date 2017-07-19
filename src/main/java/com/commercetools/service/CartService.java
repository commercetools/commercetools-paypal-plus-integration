package com.commercetools.service;

import io.sphere.sdk.carts.Cart;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * CTP service to work with {@link Cart}s
 */
public interface CartService {

    CompletionStage<Optional<Cart>> getByPaymentId(String paymentId);
}