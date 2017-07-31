package com.commercetools.helper.mapper;

import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.carts.Cart;

public interface ShippingAddressMapper {

    ShippingAddress ctpAddressToPaypalPlusAddress (Cart cart);
}
