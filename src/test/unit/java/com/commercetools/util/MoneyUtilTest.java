package com.commercetools.util;

import io.sphere.sdk.cartdiscounts.DiscountedLineItemPrice;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartShippingInfo;
import io.sphere.sdk.carts.TaxedPrice;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MoneyUtilTest {

    @Mock
    private Cart cart;

    @Test
    public void getActualShippingCost() throws Exception {
        assertThat(MoneyUtil.getActualShippingCost(null)).isEmpty();
        assertThat(MoneyUtil.getActualShippingCost(cart)).isEmpty();

        CartShippingInfo cartShippingInfo = mock(CartShippingInfo.class);

        when(cart.getShippingInfo()).thenReturn(cartShippingInfo);
        assertThat(MoneyUtil.getActualShippingCost(cart)).isEmpty();

        DiscountedLineItemPrice discountedLineItemPrice = mock(DiscountedLineItemPrice.class);

        when(cartShippingInfo.getDiscountedPrice()).thenReturn(discountedLineItemPrice);
        assertThat(MoneyUtil.getActualShippingCost(cart)).isEmpty();

        when(cartShippingInfo.getPrice()).thenReturn(Money.of(999, "UAH"));
        assertThat(MoneyUtil.getActualShippingCost(cart)).contains(Money.of(999, "UAH"));

        // discounted price has higher priority over normal price
        when(discountedLineItemPrice.getValue()).thenReturn(Money.of(666, "CZK"));
        assertThat(MoneyUtil.getActualShippingCost(cart)).contains(Money.of(666, "CZK"));
    }

    @Test
    public void getActualTax() throws Exception {
        assertThat(MoneyUtil.getActualTax(null)).isEmpty();
        assertThat(MoneyUtil.getActualTax(cart)).isEmpty();

        TaxedPrice taxedPrice = mock(TaxedPrice.class);
        when(cart.getTaxedPrice()).thenReturn(taxedPrice);
        when(taxedPrice.getTotalGross()).thenReturn(Money.of(999, "PLN"));
        when(taxedPrice.getTotalNet()).thenReturn(Money.of(666, "PLN"));

        assertThat(MoneyUtil.getActualTax(cart)).contains(Money.of(333, "PLN"));

    }

}
