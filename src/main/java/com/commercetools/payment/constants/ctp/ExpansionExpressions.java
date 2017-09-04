package com.commercetools.payment.constants.ctp;

import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.expansion.CartExpansionModel;
import io.sphere.sdk.expansion.ExpansionPathContainer;

public class ExpansionExpressions {
    public static final ExpansionPathContainer<Cart> PAYMENT_INFO_EXPANSION = CartExpansionModel.of().paymentInfo().payments();
}