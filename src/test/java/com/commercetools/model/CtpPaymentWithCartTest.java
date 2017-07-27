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

import java.util.Locale;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
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
    public void getCreditCardToken() throws Exception {
        assertThat(paymentWithCart.getCreditCardToken()).isEmpty();
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getCreditCardToken()).isEmpty();

        when(customFields.getFieldAsString(CREDIT_CARD_TOKEN)).thenReturn("blah-blah");
        assertThat(paymentWithCart.getCreditCardToken()).isEqualTo("blah-blah");
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
    public void getLocaleOrDefault() throws Exception {
        assertThat(paymentWithCart.getLocaleOrDefault()).isEqualTo(DEFAULT_LOCALE);

        CustomFields customFields = mock(CustomFields.class);

        // custom field is still empty - fallback to default.
        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getLocaleOrDefault()).isEqualTo(DEFAULT_LOCALE);

        when(cart.getLocale()).thenReturn(Locale.forLanguageTag("ua"));
        assertThat(paymentWithCart.getLocaleOrDefault()).isEqualTo(Locale.forLanguageTag("ua"));

        when(cart.getLocale()).thenReturn(Locale.CHINESE);
        assertThat(paymentWithCart.getLocaleOrDefault()).isEqualTo(Locale.CHINESE);

        // payment locale has higher priority than cart locale
        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("de");
        assertThat(paymentWithCart.getLocaleOrDefault()).isEqualTo(Locale.GERMAN);

        when(customFields.getFieldAsString(LANGUAGE_CODE_FIELD)).thenReturn("xx");
        assertThat(paymentWithCart.getLocaleOrDefault()).isEqualTo(Locale.forLanguageTag("xx"));

    }
}