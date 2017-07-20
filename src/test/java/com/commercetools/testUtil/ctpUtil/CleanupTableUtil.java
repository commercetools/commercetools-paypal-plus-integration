package com.commercetools.testUtil.ctpUtil;

import io.sphere.sdk.carts.commands.CartDeleteCommand;
import io.sphere.sdk.carts.queries.CartQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereRequest;
import io.sphere.sdk.orders.commands.OrderDeleteCommand;
import io.sphere.sdk.orders.queries.OrderQuery;
import io.sphere.sdk.payments.commands.PaymentDeleteCommand;
import io.sphere.sdk.payments.queries.PaymentQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;

public final class CleanupTableUtil {

    private static final Logger logger = LoggerFactory.getLogger(CleanupTableUtil.class);

    /**
     * Remove all items from {@code Payments} endpoint.
     *
     * @param sphereClient {@link SphereClient} where to delete Payments.
     * @return number of deleted items.
     */
    public static int cleanupPaymentTable(SphereClient sphereClient) {
        return cleanupTable(sphereClient, PaymentQuery::of, PaymentDeleteCommand::of, "Payments");
    }

    public static int cleanupOrders(SphereClient sphereClient) {
        return cleanupTable(sphereClient, OrderQuery::of, OrderDeleteCommand::of, "Orders");
    }

    public static int cleanupCarts(SphereClient sphereClient) {
        return cleanupTable(sphereClient, CartQuery::of, CartDeleteCommand::of, "Carts");
    }

    /**
     * Sequentially execute query from {@code querySupplier} to fetch items and remove them using {@code deleteFunction}
     * while query returns at least one item. Stop execution when the query returns empty result
     * (everything is removed).
     *
     * @param client         {@link SphereClient} where to execute queries
     * @param querySupplier  supplier of read {@link SphereRequest}
     * @param deleteFunction {@link Function} which accepts {@code EntityType} item and creates delete
     *                       {@link SphereRequest} for this item.
     * @param <EntityType>   type of items to read/delete
     * @return number of read/deleted items
     */
    private static <EntityType> int cleanupTable(
            @Nonnull final SphereClient client,
            @Nonnull final Supplier<SphereRequest<PagedQueryResult<EntityType>>> querySupplier,
            @Nonnull final Function<EntityType, SphereRequest<EntityType>> deleteFunction,
            @Nonnull String resourceName) {
        logger.debug("Cleanup " + resourceName + " table");

        Supplier<CompletionStage<Integer>> readDeleteStage = () -> client.execute(querySupplier.get())
                .thenApply(PagedQueryResult::getResults)
                .thenApply(results -> results.stream()
                        .map(item -> client.execute(deleteFunction.apply(item)))
                        .map(CompletionStage::toCompletableFuture)
                        .collect(Collectors.toList())
                )
                .thenApply(futuresList -> futuresList.toArray(new CompletableFuture[futuresList.size()]))
                .thenCompose(CleanupTableUtil::allOf);

        // repeat read/delete requests sequence till nothing to delete
        int removedCount = 0;
        for (int r = 0; (r = executeBlocking(readDeleteStage.get())) > 0; removedCount += r) ;

        logger.debug("Cleanup Carts table completed, removed {} items", removedCount);

        return removedCount;
    }

    /**
     * Wrap {@link CompletableFuture#allOf(CompletableFuture[])} to return not <b>void</b>, but number of processed
     * items. If {@code cfs} doesn't contain items - return completed stage with <b>0</b>
     *
     * @param cfs list of {@link CompletableFuture} to execute.
     * @return {@link CompletionStage} of all supplied {@link CompletableFuture}, or completed future with <b>0</b>
     * if nothing to process.
     */
    public static CompletionStage<Integer> allOf(final CompletableFuture<?>... cfs) {
        if (cfs != null && cfs.length > 0) {
            return CompletableFuture.allOf(cfs)
                    .thenApply(ignoreVoid -> cfs.length);
        }

        return CompletableFuture.completedFuture(0);
    }

    private CleanupTableUtil() {
    }
}
