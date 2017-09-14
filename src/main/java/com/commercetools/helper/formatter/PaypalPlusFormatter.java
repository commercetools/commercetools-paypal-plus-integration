package com.commercetools.helper.formatter;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Currency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;

public interface PaypalPlusFormatter {

    /**
     * Convert {@code monetaryAmount} to some standard string pattern.
     *
     * @param monetaryAmount value to convert.
     * @return string representation of {@code monetaryAmount} by some rules/pattern.
     */
    @Nonnull
    String monetaryAmountToString(@Nullable MonetaryAmount monetaryAmount);

    /**
     * Convert string to {@link MonetaryAmount}, accepted by CTP.
     *
     * The parser should not be depended on fractional part, e.g. "1.00" should be parsed same as "1", "1.0", "1."
     *
     * @param ppAmount paypal amount in major fractional units, like [euro.cent]
     * @param currencyCode 3 characters ISO 4217 currency code
     * @return Parsed amount. In case of error - exception is thrown.
     */
    @Nonnull
    MonetaryAmount paypalPlusAmountToCtpMonetaryAmount(@Nonnull String ppAmount, @Nonnull String currencyCode);

    /**
     * @see #paypalPlusAmountToCtpMonetaryAmount(String, String)
     */
    @Nonnull
    MonetaryAmount paypalPlusAmountToCtpMonetaryAmount(@Nonnull Amount amount);

    /**
     * @see #paypalPlusAmountToCtpMonetaryAmount(String, String)
     */
    @Nonnull
    MonetaryAmount paypalPlusAmountToCtpMonetaryAmount(@Nonnull Currency amount);

}
