package com.commercetools.helper.mapper;

import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.models.Address;

import javax.annotation.Nonnull;

public interface AddressMapper {

    /**
     * Maps CTP address to Paypal Plus shipping address
     *
     * @param ctpAddress CTP address
     * @return Paypal Plus address
     */
    ShippingAddress ctpAddressToPaypalPlusShippingAddress(@Nonnull Address ctpAddress);

    /**
     * Maps CTP address to Paypal Plus billing address
     */
    com.paypal.api.payments.Address ctpAddressToPaypalPlusBillingAddress(@Nonnull Address ctpAddress);

    /**
     * Maps CTP address to Paypal Plus /payer/payer_info
     */
    PayerInfo ctpAddressToPaypalPlusPayerInfo(@Nonnull Address ctpAddress);

}
