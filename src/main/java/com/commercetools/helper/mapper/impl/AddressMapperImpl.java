package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.mapper.AddressMapper;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.models.Address;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public ShippingAddress ctpAddressToPaypalPlusShippingAddress(@Nonnull Address ctpAddress) {
        ShippingAddress paypalAddress = new ShippingAddress();
        paypalAddress.setRecipientName(ctpAddress.getFirstName() + " " + ctpAddress.getLastName());
        paypalAddress.setLine1(ctpAddress.getStreetName());
        paypalAddress.setLine2(ctpAddress.getApartment());
        paypalAddress.setCity(ctpAddress.getCity());
        // Phone has strict verification on Paypal, for now, we don't send it
//        paypalAddress.setPhone(ctpAddress.getPhone());
        paypalAddress.setPostalCode(ctpAddress.getPostalCode());
        paypalAddress.setState(ctpAddress.getState());
        paypalAddress.setCountryCode(ctpAddress.getCountry().getAlpha2());
        return paypalAddress;
    }

    @Override
    public PayerInfo ctpAddressToPaypalPlusPayerInfo(@Nonnull Address ctpAddress) {
        return new PayerInfo()
                .setFirstName(ctpAddress.getFirstName())
                .setLastName(ctpAddress.getLastName())
                .setEmail(ctpAddress.getEmail())
                .setBillingAddress(ctpAddressToPaypalPlusBillingAddress(ctpAddress));
    }

    @Override
    public com.paypal.api.payments.Address ctpAddressToPaypalPlusBillingAddress(@Nonnull Address ctpAddress) {
        com.paypal.api.payments.Address paypalAddress = new com.paypal.api.payments.Address();
        paypalAddress.setLine1(ctpAddress.getStreetName());
        paypalAddress.setLine2(ctpAddress.getApartment());
        paypalAddress.setCity(ctpAddress.getCity());
        // Phone has strict verification on Paypal, for now, we don't send it
//        paypalAddress.setPhone(ctpAddress.getPhone());
        paypalAddress.setPostalCode(ctpAddress.getPostalCode());
        paypalAddress.setState(ctpAddress.getState());
        paypalAddress.setCountryCode(ctpAddress.getCountry().getAlpha2());
        return paypalAddress;
    }
}