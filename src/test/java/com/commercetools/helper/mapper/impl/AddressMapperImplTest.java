package com.commercetools.helper.mapper.impl;

import com.neovisionaries.i18n.CountryCode;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.models.Address;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AddressMapperImplTest {

    private AddressMapperImpl addressMapper;

    private Address address;

    @Before
    public void setUp() throws Exception {
        addressMapper = new AddressMapperImpl();
        address = Address.of(CountryCode.AC)
                .withFirstName("First")
                .withLastName("Last")
                .withEmail("xxx@yyy.zzz")
                .withStreetName("Streetname")
                .withStreetNumber("4234")
                .withApartment("234")
                .withCity("Sin-city")
                .withPostalCode("44556677")
                .withState("guggy")
                .withCountry(CountryCode.AC);
    }

    @Test
    public void ctpAddressToPaypalPlusShippingAddress() throws Exception {
        ShippingAddress shippingAddress = addressMapper.ctpAddressToPaypalPlusShippingAddress(address);
        assertThat(shippingAddress.getRecipientName()).isEqualTo("First Last");
        assertCommonAddressFields(shippingAddress);

        Address noStreetNum = address.withStreetNumber(null);
        assertThat(addressMapper.ctpAddressToPaypalPlusShippingAddress(noStreetNum).getLine1()).isEqualTo("Streetname");
        noStreetNum = address.withStreetNumber("");
        assertThat(addressMapper.ctpAddressToPaypalPlusShippingAddress(noStreetNum).getLine1()).isEqualTo("Streetname");
        noStreetNum = address.withStreetNumber("  ");
        assertThat(addressMapper.ctpAddressToPaypalPlusShippingAddress(noStreetNum).getLine1()).isEqualTo("Streetname");
    }

    @Test
    public void ctpAddressToPaypalPlusBillingAddress() throws Exception {
        com.paypal.api.payments.Address billingAddress = addressMapper.ctpAddressToPaypalPlusBillingAddress(address);
        assertCommonAddressFields(billingAddress);

        Address noStreetNum = address.withStreetNumber(null);
        assertThat(addressMapper.ctpAddressToPaypalPlusBillingAddress(noStreetNum).getLine1()).isEqualTo("Streetname");
        noStreetNum = address.withStreetNumber("");
        assertThat(addressMapper.ctpAddressToPaypalPlusBillingAddress(noStreetNum).getLine1()).isEqualTo("Streetname");
        noStreetNum = address.withStreetNumber("  ");
        assertThat(addressMapper.ctpAddressToPaypalPlusBillingAddress(noStreetNum).getLine1()).isEqualTo("Streetname");
    }

    @Test
    public void ctpAddressToPaypalPlusPayerInfo() throws Exception {
        PayerInfo payerInfo = addressMapper.ctpAddressToPaypalPlusPayerInfo(address);
        assertThat(payerInfo.getFirstName()).isEqualTo("First");
        assertThat(payerInfo.getLastName()).isEqualTo("Last");
        assertThat(payerInfo.getEmail()).isEqualTo("xxx@yyy.zzz");

        assertCommonAddressFields(payerInfo.getBillingAddress());
    }

    private void assertCommonAddressFields(com.paypal.api.payments.Address address) {
        assertThat(address.getLine1()).isEqualTo("Streetname 4234");
        assertThat(address.getLine2()).isEqualTo("234");
        assertThat(address.getCity()).isEqualTo("Sin-city");
        //assertThat(address.getPhone()).isEqualTo();
        assertThat(address.getPostalCode()).isEqualTo("44556677");
        assertThat(address.getState()).isEqualTo("guggy");
        assertThat(address.getCountryCode()).isEqualTo("AC");
    }

}