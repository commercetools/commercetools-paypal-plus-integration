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

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.CREDIT_CARD_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CtpPaymentWithCartTest {

    @Mock
    private Payment payment;

    @Mock
    private Cart cart;

    CtpPaymentWithCart paymentWithCart;

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

        when(customFields.getFieldAsString(CREDIT_CARD_TOKEN)).thenReturn("super-mario");
        assertThat(paymentWithCart.getReturnUrl()).isEqualTo("super-mario");
    }

    @Test
    public void getCancelUrl() throws Exception {
        assertThat(paymentWithCart.getCancelUrl()).isEmpty();
        CustomFields customFields = mock(CustomFields.class);

        when(payment.getCustom()).thenReturn(customFields);
        assertThat(paymentWithCart.getCancelUrl()).isEmpty();

        when(customFields.getFieldAsString(CREDIT_CARD_TOKEN)).thenReturn("WTF");
        assertThat(paymentWithCart.getCancelUrl()).isEqualTo("WTF");
    }

}