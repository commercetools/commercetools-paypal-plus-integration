package com.commercetools.testUtil;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

public final class CompletionStageUtil {

    public static <T> T executeBlocking(@Nonnull CompletionStage<T> request) {
        return request.toCompletableFuture().join();
    }

    private CompletionStageUtil() {
    }
}
