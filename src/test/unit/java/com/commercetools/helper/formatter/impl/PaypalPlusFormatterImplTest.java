package com.commercetools.helper.formatter.impl;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Currency;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PaypalPlusFormatterImpl.class)
public class PaypalPlusFormatterImplTest {

    @Autowired
    private PaypalPlusFormatterImpl formatter;

    @Test
    public void monetaryAmountToString_simpleCases() throws Exception {
        assertThat(formatter.monetaryAmountToString(null)).isEqualTo("");

        assertThat(formatter.monetaryAmountToString(Money.of(0, "EUR"))).isEqualTo("0.00");

        assertThat(formatter.monetaryAmountToString(Money.of(1, "EUR"))).isEqualTo("1.00");
        assertThat(formatter.monetaryAmountToString(Money.of(-1, "EUR"))).isEqualTo("-1.00");
        assertThat(formatter.monetaryAmountToString(Money.of(9.2, "EUR"))).isEqualTo("9.20");
        assertThat(formatter.monetaryAmountToString(Money.of(-8.3, "EUR"))).isEqualTo("-8.30");
        assertThat(formatter.monetaryAmountToString(Money.of(7.45, "EUR"))).isEqualTo("7.45");
        assertThat(formatter.monetaryAmountToString(Money.of(-6.67, "EUR"))).isEqualTo("-6.67");

        assertThat(formatter.monetaryAmountToString(Money.of(99, "EUR"))).isEqualTo("99.00");
        assertThat(formatter.monetaryAmountToString(Money.of(-98, "EUR"))).isEqualTo("-98.00");

        assertThat(formatter.monetaryAmountToString(Money.of(76.01, "EUR"))).isEqualTo("76.01");
        assertThat(formatter.monetaryAmountToString(Money.of(-54.02, "EUR"))).isEqualTo("-54.02");
        assertThat(formatter.monetaryAmountToString(Money.of(48.34, "EUR"))).isEqualTo("48.34");
        assertThat(formatter.monetaryAmountToString(Money.of(-33.56, "EUR"))).isEqualTo("-33.56");


        assertThat(formatter.monetaryAmountToString(Money.of(99.1, "EUR"))).isEqualTo("99.10");
        assertThat(formatter.monetaryAmountToString(Money.of(-99.1, "EUR"))).isEqualTo("-99.10");
    }

    @Test
    public void monetaryAmountToString_largeValues() throws Exception {
        assertThat(formatter.monetaryAmountToString(Money.of(1234567890123456789L, "EUR")))
                .isEqualTo("1234567890123456789.00");

        assertThat(formatter.monetaryAmountToString(Money.of(-1234567890123456789L, "EUR")))
                .isEqualTo("-1234567890123456789.00");

        assertThat(formatter.monetaryAmountToString(Money.of(new BigDecimal("1234567853456785456789765456789045679.78"), "EUR")))
                .isEqualTo("1234567853456785456789765456789045679.78");

        assertThat(formatter.monetaryAmountToString(Money.of(new BigDecimal("-65476512379468971263487132641238412984.1"), "EUR")))
                .isEqualTo("-65476512379468971263487132641238412984.10");

        assertThat(formatter.monetaryAmountToString(Money.of(new BigDecimal("8834579872345823452354.07"), "EUR")))
                .isEqualTo("8834579872345823452354.07");
    }

    @Test
    public void monetaryAmountToString_roundingModeIsHalfEven() throws Exception {
        assertThat(formatter.monetaryAmountToString(Money.of(0.244, "USD"))).isEqualTo("0.24");
        assertThat(formatter.monetaryAmountToString(Money.of(0.245, "USD"))).isEqualTo("0.24");
        assertThat(formatter.monetaryAmountToString(Money.of(0.24500001, "USD"))).isEqualTo("0.25");
        assertThat(formatter.monetaryAmountToString(Money.of(0.246, "USD"))).isEqualTo("0.25");

        assertThat(formatter.monetaryAmountToString(Money.of(0.274, "USD"))).isEqualTo("0.27");
        assertThat(formatter.monetaryAmountToString(Money.of(0.2749999999, "USD"))).isEqualTo("0.27");
        assertThat(formatter.monetaryAmountToString(Money.of(0.275, "USD"))).isEqualTo("0.28");
        assertThat(formatter.monetaryAmountToString(Money.of(0.276, "USD"))).isEqualTo("0.28");

        assertThat(formatter.monetaryAmountToString(Money.of(-0.244, "USD"))).isEqualTo("-0.24");
        assertThat(formatter.monetaryAmountToString(Money.of(-0.245, "USD"))).isEqualTo("-0.24");
        assertThat(formatter.monetaryAmountToString(Money.of(-0.24500001, "USD"))).isEqualTo("-0.25");
        assertThat(formatter.monetaryAmountToString(Money.of(-0.246, "USD"))).isEqualTo("-0.25");

        assertThat(formatter.monetaryAmountToString(Money.of(-0.274, "USD"))).isEqualTo("-0.27");
        assertThat(formatter.monetaryAmountToString(Money.of(-0.2749999999, "USD"))).isEqualTo("-0.27");
        assertThat(formatter.monetaryAmountToString(Money.of(-0.275, "USD"))).isEqualTo("-0.28");
        assertThat(formatter.monetaryAmountToString(Money.of(-0.276, "USD"))).isEqualTo("-0.28");
    }


    @Test
    public void paypalPlusAmountToCtpMonetaryAmount_simpleCases() throws Exception {
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("0.00", "EUR")).isEqualTo(Money.of(0, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("0", "EUR")).isEqualTo(Money.of(0, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-0", "EUR")).isEqualTo(Money.of(0, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-0.0", "EUR")).isEqualTo(Money.of(0, "EUR"));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1.00", "USD")).isEqualTo(Money.of(1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1.0", "USD")).isEqualTo(Money.of(1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1.", "USD")).isEqualTo(Money.of(1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1.1", "USD")).isEqualTo(Money.of(1.1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1.10", "USD")).isEqualTo(Money.of(1.1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1.01", "USD")).isEqualTo(Money.of(1.01, "USD"));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1.00", "USD")).isEqualTo(Money.of(-1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1.0", "USD")).isEqualTo(Money.of(-1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1.", "USD")).isEqualTo(Money.of(-1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1.1", "USD")).isEqualTo(Money.of(-1.1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1.10", "USD")).isEqualTo(Money.of(-1.1, "USD"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1.01", "USD")).isEqualTo(Money.of(-1.01, "USD"));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("9.2", "UAH")).isEqualTo(Money.of(9.2, "UAH"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-8.3", "EUR")).isEqualTo(Money.of(-8.3, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("7.45", "EUR")).isEqualTo(Money.of(7.45, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-6.67", "EUR")).isEqualTo(Money.of(-6.67, "EUR"));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("99", "EUR")).isEqualTo(Money.of(99, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-98", "EUR")).isEqualTo(Money.of(-98, "EUR"));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("76.01", "EUR")).isEqualTo(Money.of(76.01, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-54.02", "EUR")).isEqualTo(Money.of(-54.02, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("48.34", "EUR")).isEqualTo(Money.of(48.34, "EUR"));
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-33.56", "EUR")).isEqualTo(Money.of(-33.56, "EUR"));
    }

    @Test
    public void paypalPlusAmountToCtpMonetaryAmount_largeValues() throws Exception {
        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1234567890123456789", "CZK"))
                .isEqualTo(Money.of(1234567890123456789L, "CZK"));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-1234567890123456789", "EUR")
                .isEqualTo(Money.of(-1234567890123456789L, "EUR")));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("1234567853456785456789765456789045679.78", "USD")
                .isEqualTo(Money.of(new BigDecimal("1234567853456785456789765456789045679.78"), "USD")));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("-65476512379468971263487132641238412984.10", "DOP")
                .isEqualTo(Money.of(new BigDecimal("-65476512379468971263487132641238412984.1"), "DOP")));

        assertThat(formatter.paypalPlusAmountToCtpMonetaryAmount("8834579872345823452354.07", "USD")
                .isEqualTo(Money.of(new BigDecimal("8834579872345823452354.07"), "USD")));
    }

    /**
     * Verify the overloads of
     * {@link PaypalPlusFormatterImpl#paypalPlusAmountToCtpMonetaryAmount(java.lang.String, java.lang.String)}
     * call this method with currency+amount arguments.
     *
     * While this test is success - we don't need separate test cases for the overloaded methods, since they use
     * the same implementation.
     */
    @Test
    public void paypalPlusAmountToCtpMonetaryAmount_overloads() throws Exception {
        // verify Amount overload
        // PaypalPlusFormatterImpl#paypalPlusAmountToCtpMonetaryAmount(com.paypal.api.payments.Amount)
        PaypalPlusFormatterImpl spiedFormatter = spy(formatter);
        spiedFormatter.paypalPlusAmountToCtpMonetaryAmount(new Amount("USD", "22.15"));
        ArgumentCaptor<String> amountCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> currencyCaptor = ArgumentCaptor.forClass(String.class);
        verify(spiedFormatter).paypalPlusAmountToCtpMonetaryAmount(amountCaptor.capture(), currencyCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualTo("22.15");
        assertThat(currencyCaptor.getValue()).isEqualTo("USD");

        // verify Currency overload
        // PaypalPlusFormatterImpl#paypalPlusAmountToCtpMonetaryAmount(com.paypal.api.payments.Currency)
        spiedFormatter = spy(formatter);
        spiedFormatter.paypalPlusAmountToCtpMonetaryAmount(new Currency("EUR", "-45.18"));
        amountCaptor = ArgumentCaptor.forClass(String.class);
        currencyCaptor = ArgumentCaptor.forClass(String.class);
        verify(spiedFormatter).paypalPlusAmountToCtpMonetaryAmount(amountCaptor.capture(), currencyCaptor.capture());
        assertThat(amountCaptor.getValue()).isEqualTo("-45.18");
        assertThat(currencyCaptor.getValue()).isEqualTo("EUR");
    }


}