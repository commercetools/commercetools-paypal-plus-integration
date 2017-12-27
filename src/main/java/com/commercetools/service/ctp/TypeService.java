package com.commercetools.service.ctp;

import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.types.FieldDefinition;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.TypeDraft;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface TypeService {
    CompletionStage<List<Type>> getTypes();

    CompletionStage<Type> createType(@Nonnull TypeDraft typeDraft);

    CompletionStage<Type> addFieldDefinitions(@Nonnull Type type, @Nonnull List<FieldDefinition> fieldDefinitions);

    CompletionStage<Type> updateType(@Nonnull Type type, @Nonnull List<UpdateAction<Type>> updateActions);
}
