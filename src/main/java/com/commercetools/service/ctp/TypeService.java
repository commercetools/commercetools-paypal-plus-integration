package com.commercetools.service.ctp;

import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.TypeDraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TypeService {
    CompletionStage<List<Type>> getTypes();

    CompletionStage<Type> createType(@Nonnull TypeDraft typeDraft);

    CompletionStage<Type> updateType(@Nonnull Type type, @Nullable List<UpdateAction<Type>> updateActions);
}
