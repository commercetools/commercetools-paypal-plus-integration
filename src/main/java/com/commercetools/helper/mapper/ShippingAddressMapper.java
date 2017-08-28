package com.commercetools.helper.mapper;

import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.models.Address;

public interface ShippingAddressMapper {

    ShippingAddress ctpAddressToPaypalPlusAddress(Address ctpAddress);
}
