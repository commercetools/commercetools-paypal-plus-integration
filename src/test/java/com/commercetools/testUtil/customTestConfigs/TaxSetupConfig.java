package com.commercetools.testUtil.customTestConfigs;

import com.neovisionaries.i18n.CountryCode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.taxcategories.TaxCategoryDraft;
import io.sphere.sdk.taxcategories.TaxRateDraft;
import io.sphere.sdk.taxcategories.commands.TaxCategoryCreateCommand;
import io.sphere.sdk.taxcategories.queries.TaxCategoryQuery;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;

public class TaxSetupConfig {

    @Autowired
    private SphereClient sphereClient;

    public static final String TAX_CATEGORY_NAME = "testTaxCategory";

    @PostConstruct
    void init() {
        executeBlocking(sphereClient.execute(TaxCategoryQuery.of().plusPredicates(m -> m.name().is(TAX_CATEGORY_NAME)))
                .thenApply(r -> {
                    if (r.getTotal() == 0) {
                        TaxCategoryDraft draft = TaxCategoryDraft.of(TAX_CATEGORY_NAME,
                                Collections.singletonList(TaxRateDraft.of("testTaxRate", 0.1, true, CountryCode.DE))
                        );
                        return sphereClient.execute(TaxCategoryCreateCommand.of(draft));
                    }
                    return CompletableFuture.completedFuture(null);
                }));
    }
}