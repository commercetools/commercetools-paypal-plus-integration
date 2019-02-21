package com.commercetools.testUtil.ctpUtil;

import com.neovisionaries.i18n.CountryCode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.TaxCategoryDraft;
import io.sphere.sdk.taxcategories.TaxRateDraft;
import io.sphere.sdk.taxcategories.commands.TaxCategoryCreateCommand;
import io.sphere.sdk.taxcategories.queries.TaxCategoryQuery;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;

public final class TaxUtil {

    public static final String TAX_CATEGORY_NAME = "testTaxCategory";

    /**
     * Create a new tax category in the CT project if not exist
     * @param sphereClient
     * @return
     */
    public static TaxCategory ensureTestTaxCategory(SphereClient sphereClient) {
        return executeBlocking(sphereClient.execute(TaxCategoryQuery.of().plusPredicates(m -> m.name().is(TAX_CATEGORY_NAME)))
                .thenApply(r -> {
                    if (r.getTotal() == 0) {
                        TaxCategoryDraft draft = TaxCategoryDraft.of(TAX_CATEGORY_NAME,
                                Collections.singletonList(TaxRateDraft.of("testTaxRate", 0.1, true, CountryCode.DE))
                        );
                        return sphereClient.execute(TaxCategoryCreateCommand.of(draft)).toCompletableFuture().join();
                    }
                    return sphereClient.execute(
                            TaxCategoryQuery.of().plusPredicates(m -> m.name().is(TAX_CATEGORY_NAME)))
                            .toCompletableFuture().join().head().get();
                }));
    }

    private TaxUtil() {
    }
}
