package com.commercetools.testUtil.ctpUtil;

import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.payments.Payment;

import static io.sphere.sdk.json.SphereJsonUtils.readObjectFromResource;

/**
 * Utility to create CT platform object instances.
 */
public final class CtpResourcesUtil {

    public static Cart getCartFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, Cart.class);
    }

    public static CartDraft getCartDraftFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, CartDraft.class);
    }

    public static Payment getPaymentFromResource(String resourcePath) {
        return readObjectFromResource(resourcePath, Payment.class);
    }

    private CtpResourcesUtil() {
    }
}