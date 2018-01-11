package com.commercetools.service.ctp.impl;

import com.commercetools.service.ctp.TypeService;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.TypeDraft;
import io.sphere.sdk.types.commands.TypeCreateCommand;
import io.sphere.sdk.types.commands.TypeUpdateCommand;
import io.sphere.sdk.types.queries.TypeQueryBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static io.sphere.sdk.queries.QueryExecutionUtils.queryAll;

public class TypeServiceImpl extends BaseSphereService implements TypeService {
    public TypeServiceImpl(@Nonnull SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<List<Type>> getTypes() {
        return queryAll(sphereClient, TypeQueryBuilder.of().build());
    }

    @Override
    public CompletionStage<Type> createType(@Nonnull TypeDraft typeDraft) {
        return sphereClient.execute(TypeCreateCommand.of(typeDraft));
    }

    @Override
    public CompletionStage<Type> updateType(@Nonnull Type type, @Nonnull List<UpdateAction<Type>> updateActions) {
        return sphereClient.execute(TypeUpdateCommand.of(type, updateActions));
    }


}
