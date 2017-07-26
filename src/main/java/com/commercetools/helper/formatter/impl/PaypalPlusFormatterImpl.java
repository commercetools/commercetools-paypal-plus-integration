package com.commercetools.helper.formatter.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQuery;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;

import static java.util.Locale.US;

public class PaypalPlusFormatterImpl implements PaypalPlusFormatter {


    /**
     * Get a formatter which converts a {@link MonetaryAmount} to a string as
     * {@code "(Unlimited integer part).(2 mandatory digits)"}
     * <p>
     * Since {@link org.javamoney.moneta.internal.format.DefaultMonetaryAmountFormat} is not thread-safe - we don't
     * share the instance, but create a new one for each access.
     */
    private static MonetaryAmountFormat getPaypalPlusMonetaryFormat() {
        return MonetaryFormats.getAmountFormat(
                AmountFormatQuery.of(US).toBuilder()
                        .set("pattern", "0.00")
                        .build());
    }

    /**
     * Format the {@code monetaryAmount} to string with 2 fractional digit and a period as fraction separator.
     * <p>
     * <b>null</b> is converted to empty string.
     * <p>
     * Examples:
     * <ul>
     * <li>{@code 0 -> "0.00"}</li>
     * <li>{@code 1 -> "1.00"}</li>
     * <li>{@code 5.6 -> "5.60"}</li>
     * <li>{@code -3.24 -> "-3.24"}</li>
     * <li>{@code null -> ""}</li>
     * </ul>
     * <p>
     * Rounding mode is <i>half to even</i>, e.g.:
     * <ul>
     * <li>{@code 1.235 -> 1.24}</li>
     * <li>{@code 1.245 -> 1.24}</li>
     * <li>{@code -1.235 -> -1.24}</li>
     * <li>{@code -1.245 -> -1.24}</li>
     * </ul>
     *
     * @param monetaryAmount value to convert to string
     * @return string with 2 fractional digits after period, or empty string for <b>null</b>
     */
    @Nonnull
    @Override
    public String monetaryAmountToString(@Nullable MonetaryAmount monetaryAmount) {
        return monetaryAmount == null ? "" : getPaypalPlusMonetaryFormat().queryFrom(monetaryAmount);
    }
}
