package com.commercetools.testUtil.ctpUtil;

import com.commercetools.model.CtpPaymentWithCart;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.payments.Payment;

import static com.commercetools.testUtil.ResourcesUtil.resolveMockDataResource;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getCartFromResource;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getPaymentFromResource;

/**
 * Read mock data from the JSON resources for the unit tests.
 */
public final class UnitCtpResourcesUtil {

    public static Payment getDummyPaymentForComplexCartWithDiscounts() {
        return getPaymentFromResource(resolveMockDataResource("paymentMapper/dummyPaymentForComplexCartWithDiscounts.json"));
    }

    public static Payment getDummyPaymentForComplexCartWithoutDiscounts() {
        return getPaymentFromResource(resolveMockDataResource("paymentMapper/dummyPaymentForComplexCartWithoutDiscounts.json"));
    }

    public static Cart getDummyComplexCartWithDiscounts() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithDiscounts.json"));
    }

    /**
     * @return Cart with discounts different shipping and billing addresses.
     */
    public static Cart getDummyComplexCartWithDiscountsShippingBillingAddress() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithDiscountsShippingBillingAddress.json"));
    }

    public static Cart getDummyComplexCartWithoutDiscounts() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithoutDiscounts.json"));
    }

    /**
     * @return Cart without discounts and with different shipping and billing addresses.
     */
    public static Cart getDummyComplexCartWithoutDiscountsShippingBillingAddress() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithoutDiscountsShippingBillingAddress.json"));
    }

    public static CtpPaymentWithCart getPaymentWithCart_complexAndDiscount() {
        return new CtpPaymentWithCart(getDummyPaymentForComplexCartWithDiscounts(), getDummyComplexCartWithDiscounts());
    }

    public static CtpPaymentWithCart getPaymentWithCart_complexWithoutDiscount() {
        return new CtpPaymentWithCart(getDummyPaymentForComplexCartWithoutDiscounts(), getDummyComplexCartWithoutDiscounts());
    }

    private UnitCtpResourcesUtil() {
    }
}
