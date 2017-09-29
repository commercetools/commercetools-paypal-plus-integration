package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.mapper.AddressMapper;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.models.Address;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static java.util.Optional.ofNullable;

@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public ShippingAddress ctpAddressToPaypalPlusShippingAddress(@Nonnull Address ctpAddress) {
        ShippingAddress paypalAddress = new ShippingAddress();
        paypalAddress.setRecipientName(ctpAddress.getFirstName() + " " + ctpAddress.getLastName());
        return populateCommonAddressFields(ctpAddress, paypalAddress);
    }

    @Override
    public com.paypal.api.payments.Address ctpAddressToPaypalPlusBillingAddress(@Nonnull Address ctpAddress) {
        return populateCommonAddressFields(ctpAddress, new com.paypal.api.payments.Address());
    }

    private <A extends com.paypal.api.payments.Address> A populateCommonAddressFields(
            @Nonnull Address ctpAddress,
            @Nonnull A paypalAddress) {
        paypalAddress.setLine1(generateLine1(ctpAddress));

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

    /**
     * @param ctpAddress CTP {@link Address} to read
     * @return {@link Address#streetName} optionally followed by {@link Address#streetNumber}, if the street number is
     * not blank.
     */
    private String generateLine1(@Nonnull Address ctpAddress) {
        return ctpAddress.getStreetName() +
                ofNullable(ctpAddress.getStreetNumber())
                        .filter(StringUtils::isNotBlank)
                        .map(n -> " " + n)
                        .orElse("");
    }

}