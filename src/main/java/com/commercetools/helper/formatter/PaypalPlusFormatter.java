package com.commercetools.helper.formatter;

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

}
