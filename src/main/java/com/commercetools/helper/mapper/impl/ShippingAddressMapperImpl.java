package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.mapper.ShippingAddressMapper;
import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.models.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShippingAddressMapperImpl implements ShippingAddressMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ShippingAddressMapperImpl.class);

    @Override
    public ShippingAddress ctpAddressToPaypalPlusAddress(Address ctpAddress) {
        ShippingAddress paypalAddress = new ShippingAddress();
        paypalAddress.setRecipientName(ctpAddress.getLastName());
        paypalAddress.setLine1(ctpAddress.getStreetName());
        paypalAddress.setLine2(ctpAddress.getApartment());
        paypalAddress.setCity(ctpAddress.getCity());
        paypalAddress.setPhone(ctpAddress.getPhone());
        paypalAddress.setPostalCode(ctpAddress.getPostalCode());
        paypalAddress.setState(ctpAddress.getState());
        paypalAddress.setCountryCode(ctpAddress.getCountry().getAlpha2());
        return paypalAddress;
    }
}