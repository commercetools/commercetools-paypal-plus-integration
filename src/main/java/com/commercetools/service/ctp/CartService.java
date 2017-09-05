package com.commercetools.service.ctp;

import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.expansion.ExpansionPathContainer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * CTP service to work with {@link Cart}s
 */
public interface CartService {

    CompletionStage<Optional<Cart>> getByPaymentId(@Nullable String paymentId);

    CompletionStage<Optional<Cart>> getByPaymentId(@Nullable String paymentId,
                                                   @Nullable ExpansionPathContainer<Cart> pathContainer);

    CompletionStage<Optional<Cart>> getByPaymentMethodAndInterfaceId(@Nullable String paymentMethodInterface,
                                                                     @Nullable String interfaceId);

    CompletionStage<Optional<Cart>> getByPaymentMethodAndInterfaceId(@Nullable String paymentMethodInterface,
                                                                     @Nullable String interfaceId,
                                                                     @Nullable ExpansionPathContainer<Cart> pathContainer);
}