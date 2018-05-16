package com.commercetools.model;

import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;
import io.sphere.sdk.types.CustomFields;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static java.util.Locale.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CtpPaymentWithCartTest {

    @Mock
    private Payment payment;

    @Mock
    private Cart cart;

    private CtpPaymentWithCart paymentWithCart;

    @Before
    public void setUp() throws Exception {
        paymentWithCart = new CtpPaymentWithCart(payment, cart);
    }

    @Test
    public void getPaymentGetCart() throws Exception {
        assertThat(paymentWithCart.getPayment()).isSameAs(payment);
        assertThat(paymentWithCart.getCart()).isSameAs(cart);
    }

    @Test
    public void getPaymentMethod() throws Exception {
        assertThat(paymentWithCart.getPaymentMethod()).isEmpty();
        PaymentMethodInfo paymentMethodInfo = mock(PaymentMethodInfo.class);

        when(payment.getPaymentMethodInfo()).thenReturn(paymentMethodInfo);
        assertThat(paymentWithCart.getPaymentMethod()).isEmpty();

        when(paymentMethodInfo.getMethod()).thenReturn("OMG");
        assertThat(paymentWithCart.getPaymentMethod()).isEqualTo("OMG");
    }

    @Test
    public void getReturnUrl() throws Exception {
        assertThat(paymentWithCart.getReturnUrl()).isEmpty();
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getReturnUrl()).isEmpty();

        when(customFields.getFieldAsString(SUCCESS_URL_FIELD)).thenReturn("super-mario");
        assertThat(paymentWithCart.getReturnUrl()).isEqualTo("super-mario");
    }

    @Test
    public void getCancelUrl() throws Exception {
        assertThat(paymentWithCart.getCancelUrl()).isEmpty();
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getCancelUrl()).isEmpty();

        when(customFields.getFieldAsString(CANCEL_URL_FIELD)).thenReturn("WTF");
        assertThat(paymentWithCart.getCancelUrl()).isEqualTo("WTF");
    }

    @Test
    public void getExperienceProfileId() throws Exception {
        assertThat(paymentWithCart.getExperienceProfileId()).isNull();
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getExperienceProfileId()).isNull();

        when(customFields.getFieldAsString(EXPERIENCE_PROFILE_ID)).thenReturn("WTF");
        assertThat(paymentWithCart.getExperienceProfileId()).isEqualTo("WTF");
    }

    @Test
    public void getShippingPreference() throws Exception {
        assertThat(paymentWithCart.getShippingPreference()).isNull();
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getShippingPreference()).isNull();

        when(customFields.getFieldAsEnumKey(SHIPPING_PREFERENCE)).thenReturn("WATT");
        assertThat(paymentWithCart.getShippingPreference()).isEqualTo("WATT");
    }

    @Test
    public void getTransactionDescription() throws Exception {
        assertThat(paymentWithCart.getTransactionDescription()).isEqualTo("Reference: ");
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getTransactionDescription()).isEqualTo("Reference: ");

        when(customFields.getFieldAsString(REFERENCE)).thenReturn("XXX-999");
        assertThat(paymentWithCart.getTransactionDescription()).isEqualTo("Reference: XXX-999");

        // ".description" has priority over ".reference"
        when(customFields.getFieldAsString(DESCRIPTION)).thenReturn("Custom description");
        assertThat(paymentWithCart.getTransactionDescription()).isEqualTo("Custom description");
    }

    @Test
    public void getLocalesWithDefault() throws Exception {
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(DEFAULT_LOCALE);

        CustomFields customFields = mock(CustomFields.class);

        // custom field is still empty - fallback to default.
        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(DEFAULT_LOCALE);

        when(cart.getLocale()).thenReturn(forLanguageTag("ua"));
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(forLanguageTag("ua"), DEFAULT_LOCALE);

        when(cart.getLocale()).thenReturn(CHINESE);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(CHINESE, DEFAULT_LOCALE);

        // payment locale has higher priority than cart locale
        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("de");
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(GERMAN, CHINESE, DEFAULT_LOCALE);

        when(cart.getLocale()).thenReturn(GERMAN);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(GERMAN, DEFAULT_LOCALE);
    }

    @Test
    public void getLocalesWithDefault_doesNotHaveDuplicates() throws Exception {
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(DEFAULT_LOCALE);

        CustomFields customFields = mock(CustomFields.class);
        when(payment.getCustom()).thenReturn(customFields);

        when(cart.getLocale()).thenReturn(DEFAULT_LOCALE);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(DEFAULT_LOCALE);

        // both
        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn(DEFAULT_LOCALE.getLanguage());
        when(cart.getLocale()).thenReturn(DEFAULT_LOCALE);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(DEFAULT_LOCALE);

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("xx");
        when(cart.getLocale()).thenReturn(DEFAULT_LOCALE);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(forLanguageTag("xx"), DEFAULT_LOCALE);

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("en");
        when(cart.getLocale()).thenReturn(DEFAULT_LOCALE);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(DEFAULT_LOCALE);

        // both cart and payment contain the same locale:
        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("xx");
        when(cart.getLocale()).thenReturn(forLanguageTag("xx"));
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(forLanguageTag("xx"), DEFAULT_LOCALE);

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("xx");
        when(cart.getLocale()).thenReturn(DEFAULT_LOCALE);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(forLanguageTag("xx"), DEFAULT_LOCALE); // payment has xx, cart is default

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("xx");
        when(cart.getLocale()).thenReturn(FRENCH);
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(forLanguageTag("xx"), FRENCH, DEFAULT_LOCALE);

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn(ENGLISH.getLanguage());
        when(cart.getLocale()).thenReturn(forLanguageTag("xx"));
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(ENGLISH, forLanguageTag("xx")); // payment has en (default), cart has xx

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn(CHINESE.getLanguage());
        when(cart.getLocale()).thenReturn(forLanguageTag("xx"));
        assertThat(paymentWithCart.getLocalesWithDefault()).containsExactly(CHINESE, forLanguageTag("xx"), DEFAULT_LOCALE);
    }

}