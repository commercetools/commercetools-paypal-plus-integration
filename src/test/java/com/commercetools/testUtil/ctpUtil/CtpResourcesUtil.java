package com.commercetools.testUtil.ctpUtil;

import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.CustomLineItemDraft;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.taxcategories.TaxCategory;
import org.javamoney.moneta.Money;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;

/**
 * Utility to create CT platform object instances.
 */
public class CtpResourcesUtil {

    public static CartDraft getCartDraftWithCustomLineItems(TaxCategory taxCategory) {
        CustomLineItemDraft testItem = CustomLineItemDraft.of(
                LocalizedString.of(Locale.GERMANY, "testItem"),
                "/test",
                Money.of(1, EUR),
                taxCategory,
                10);

        return getCartDraft(Collections.singletonList(testItem));
    }

    public static CartDraft getCartDraft(List<CustomLineItemDraft> customLineItemsDrafts) {
        return CartDraftBuilder.of(getCartDraft()).customLineItems(customLineItemsDrafts).build();
    }

    private static CartDraft getCartDraft() {
        return SphereJsonUtils.readObjectFromResource("mockdata/cartDraft.json", CartDraft.class);
    }
}