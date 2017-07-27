package com.commercetools.testUtil.ctpUtil;

import com.commercetools.model.CtpPaymentWithCart;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.CustomLineItemDraft;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.taxcategories.TaxCategory;
import org.javamoney.moneta.Money;

import java.util.List;
import java.util.Locale;

import static io.sphere.sdk.json.SphereJsonUtils.readObjectFromResource;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.util.Collections.singletonList;

/**
 * Utility to create CT platform object instances.
 */
public class CtpResourcesUtil {

    public static String MOCK_ROOT_DIR = "mockData/";

    public static CartDraft getCartDraftWithCustomLineItems(TaxCategory taxCategory) {
        CustomLineItemDraft testItem = CustomLineItemDraft.of(
                LocalizedString.of(Locale.GERMANY, "testItem"),
                "/test",
                Money.of(1, EUR),
                taxCategory,
                10);

        return getCartDraft(singletonList(testItem));
    }

    public static CartDraft getCartDraft(List<CustomLineItemDraft> customLineItemsDrafts) {
        return CartDraftBuilder.of(getCartDraft()).customLineItems(customLineItemsDrafts).build();
    }

    private static CartDraft getCartDraft() {
        return getCartDraftFromResource(resolveMockDataResource("cartDraft.json"));
    }

    public static Payment getDummyPaymentForComplexCartWithDiscounts() {
        return getPaymentFromResource(resolveMockDataResource("paymentMapper/dummyPaymentForComplexCartWithDiscounts.json"));
    }

    public static Payment getDummyPaymentForComplexCartWithoutDiscounts() {
        return getPaymentFromResource(resolveMockDataResource("paymentMapper/dummyPaymentForComplexCartWithoutDiscounts.json"));
    }

    public static Cart getDummyComplexCartWithDiscounts() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithDiscounts.json"));
    }

    public static Cart getDummyComplexCartWithoutDiscounts() {
        return getCartFromResource(resolveMockDataResource("paymentMapper/dummyComplexCartWithoutDiscounts.json"));
    }

    public static CtpPaymentWithCart getPaymentWithCart_complexAndDiscount() {
        return new CtpPaymentWithCart(getDummyPaymentForComplexCartWithDiscounts(), getDummyComplexCartWithDiscounts());
    }

    public static CtpPaymentWithCart getPaymentWithCart_complexWithoutDiscount() {
        return new CtpPaymentWithCart(getDummyPaymentForComplexCartWithoutDiscounts(), getDummyComplexCartWithoutDiscounts());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Common util methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Payment getPaymentFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, Payment.class);
    }

    public static Cart getCartFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, Cart.class);
    }

    public static CartDraft getCartDraftFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, CartDraft.class);
    }

    public static String resolveMockDataResource(String mockDataRelativePath) {
        return MOCK_ROOT_DIR + mockDataRelativePath;
    }
}