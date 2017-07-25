package com.commercetools.helper.formatter.impl;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class PaypalPlusFormatterImplTest {

    private PaypalPlusFormatterImpl formatter = new PaypalPlusFormatterImpl();

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
}