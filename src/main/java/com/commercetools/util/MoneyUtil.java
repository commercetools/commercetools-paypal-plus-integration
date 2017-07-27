package com.commercetools.util;

import io.sphere.sdk.cartdiscounts.DiscountedLineItemPrice;
import io.sphere.sdk.carts.CartLike;
import io.sphere.sdk.carts.CartShippingInfo;
import io.sphere.sdk.carts.TaxedPrice;

import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Payments/prices/discounts calculation and expanding utils.
 */
public final class MoneyUtil {

    /**
     * From {@link CartLike#getShippingInfo()} try to fetch <b><i>real</i></b> paying shipping cost, including all
     * discounts.
     *
     * @param cartLike {@link CartLike} to parse
     * @return first existent item in the following order:<ol>
     * <li><code>{@link CartShippingInfo#getDiscountedPrice()}.getValue()</code></li>
     * <li>otherwise <code>{@link CartShippingInfo#getPrice()}</code></li>
     * <li>otherwise empty</li>
     * </ol>
     */
    public static Optional<MonetaryAmount> getActualShippingCost(@Nullable CartLike cartLike) {
        return ofNullable(cartLike)
                .map(CartLike::getShippingInfo)
                .map(cartShippingInfo -> ofNullable(cartShippingInfo.getDiscountedPrice())
                        .map(DiscountedLineItemPrice::getValue)
                        .orElseGet(cartShippingInfo::getPrice));
    }

    /**
     * From {@link CartLike#getTaxedPrice()} try to fetch tax amount value, if available.
     *
     * @param cartLike {@link CartLike} to parse
     * @return if {@link CartLike#getTaxedPrice()} exists - subtraction of
     * <code>{@link TaxedPrice#getTotalGross()} - {@link TaxedPrice#getTotalNet()}</code>. Otherwise - empty.
     */
    public static Optional<MonetaryAmount> getActualTax(@Nullable CartLike cartLike) {
        return ofNullable(cartLike)
                .map(CartLike::getTaxedPrice)
                .map(taxedPrice -> taxedPrice.getTotalGross().subtract(taxedPrice.getTotalNet()));
    }


    private MoneyUtil() {
    }
}
