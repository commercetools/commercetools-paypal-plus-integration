package com.commercetools.service.ctp.impl;

import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.commands.UpdateCommand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

abstract public class BaseSphereService {

    @Nonnull
    final SphereClient sphereClient;

    protected BaseSphereService(@Nonnull SphereClient sphereClient) {
        this.sphereClient = sphereClient;
    }

    /**
     * Optimize Sphere update actions requests: if {@code updateActions} list is empty (null or doesn't contain values)
     * &mdash; don't send sphere request and return completed stage with the same {@code entity} instance
     *
     * @param entity                instance of CTP platform value to update
     * @param updateActions         list of actions to apply. If null or empty - sphere request won't be sent
     * @param updateCommandSupplier function to generate Sphere {@link UpdateCommand} based on {@code entity} and
     *                              {@code updateActions} if the actions list is not empty
     * @param <T>                   type of CTP platform entity
     * @return completed future with {@code entity} instance, if {@code updateActions} list doesn't
     * have values to update. Otherwise - make regular sphere client request
     * and return completion stage with updated entity instance.
     */
    protected final <T extends io.sphere.sdk.models.Resource<T>> CompletionStage<T>
    returnSameInstanceIfEmptyListOrExecuteCommand(
            @Nonnull T entity,
            @Nullable List<? extends UpdateAction<T>> updateActions,
            @Nonnull BiFunction<T, List<? extends UpdateAction<T>>, UpdateCommand<T>> updateCommandSupplier) {

        return isEmpty(updateActions)
                ? completedFuture(entity)
                : sphereClient.execute(updateCommandSupplier.apply(entity, updateActions));
    }
}
