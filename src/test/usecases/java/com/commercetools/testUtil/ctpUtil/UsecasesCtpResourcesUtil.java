package com.commercetools.testUtil.ctpUtil;

import io.sphere.sdk.carts.CartDraft;

import static com.commercetools.testUtil.ResourcesUtil.resolveMockDataResource;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getCartDraftFromResource;

/**
 * Read mock data from the JSON resources for the usecases tests.
 */
public final class UsecasesCtpResourcesUtil {

    public static CartDraft getUTDummyComplexCartDraftWithDiscounts() {
        return getCartDraftFromResource(resolveMockDataResource("testPayments/dummyCartDraftWithDiscounts_UT.json"));
    }

    private UsecasesCtpResourcesUtil() {
    }
}
