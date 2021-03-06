package com.commercetools.testUtil.ctpUtil;

import com.commercetools.pspadapter.facade.SphereClientFactory;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import io.sphere.sdk.carts.commands.CartDeleteCommand;
import io.sphere.sdk.carts.queries.CartQuery;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereRequest;
import io.sphere.sdk.orders.commands.OrderDeleteCommand;
import io.sphere.sdk.orders.queries.OrderQuery;
import io.sphere.sdk.payments.commands.PaymentDeleteCommand;
import io.sphere.sdk.payments.queries.PaymentQuery;
import io.sphere.sdk.products.commands.ProductDeleteCommand;
import io.sphere.sdk.products.queries.ProductQuery;
import io.sphere.sdk.producttypes.commands.ProductTypeDeleteCommand;
import io.sphere.sdk.producttypes.queries.ProductTypeQuery;
import io.sphere.sdk.queries.PagedQueryResult;
import io.sphere.sdk.shippingmethods.commands.ShippingMethodDeleteCommand;
import io.sphere.sdk.shippingmethods.queries.ShippingMethodQuery;
import io.sphere.sdk.taxcategories.commands.TaxCategoryDeleteCommand;
import io.sphere.sdk.taxcategories.queries.TaxCategoryQuery;
import io.sphere.sdk.types.commands.TypeDeleteCommand;
import io.sphere.sdk.types.queries.TypeQuery;
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
    public static int cleanupPayments(SphereClient sphereClient) {
        return cleanupTable(sphereClient, PaymentQuery::of, PaymentDeleteCommand::of, "Payments");
    }

    public static int cleanupOrders(SphereClient sphereClient) {
        return cleanupTable(sphereClient, OrderQuery::of, OrderDeleteCommand::of, "Orders");
    }

    public static int cleanupCarts(SphereClient sphereClient) {
        return cleanupTable(sphereClient, CartQuery::of, CartDeleteCommand::of, "Carts");
    }

    public static int cleanupTypes(SphereClient sphereClient) {
        return cleanupTable(sphereClient, TypeQuery::of, TypeDeleteCommand::of, "Types");
    }

    public static int cleanupTaxCategories(SphereClient sphereClient) {
        return cleanupTable(sphereClient, TaxCategoryQuery::of, TaxCategoryDeleteCommand::of, "TaxCategories");
    }

    public static int cleanupShippingMethods(SphereClient sphereClient) {
        return cleanupTable(sphereClient, ShippingMethodQuery::of, ShippingMethodDeleteCommand::of, "ShippingMethods");
    }

    public static int cleanupProducts(SphereClient sphereClient) {
        return cleanupTable(sphereClient, ProductQuery::of, ProductDeleteCommand::of, "Products");
    }

    public static int cleanupProductTypes(SphereClient sphereClient) {
        return cleanupTable(sphereClient, ProductTypeQuery::of, ProductTypeDeleteCommand::of, "ProductTypes");
    }
    /**
     * Wipe out all types from all tenants.
     */
    public static void cleanupAllTenantsTypes(TenantConfigFactory tenantConfigFactory, SphereClientFactory sphereClientFactory) {
        tenantConfigFactory.getTenantConfigs().parallelStream()
                .map(sphereClientFactory::createSphereClient)
                .forEach(CleanupTableUtil::cleanupOrdersCartsPaymentsTypes);
    }

    public static void cleanupOrdersCartsPayments(SphereClient sphereClient) {
        cleanupOrders(sphereClient);
        cleanupCarts(sphereClient);
        cleanupPayments(sphereClient);
    }

    public static void cleanupOrdersCartsPaymentsTypes(SphereClient sphereClient) {
        cleanupOrdersCartsPayments(sphereClient);
        cleanupTypes(sphereClient);
    }

    public static void cleanupProductsProductTypesTaxCategories(SphereClient sphereClient){
        cleanupProducts(sphereClient);
        cleanupProductTypes(sphereClient);
        cleanupShippingMethods(sphereClient);
        cleanupTaxCategories(sphereClient);
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
     * @param resourceName   name of the cleaned CTP resource to display in the debug messages
     * @param <EntityType>   type of items to read/delete
     * @return number of read/deleted items
     */
    private static <EntityType> int cleanupTable(
            @Nonnull final SphereClient client,
            @Nonnull final Supplier<SphereRequest<PagedQueryResult<EntityType>>> querySupplier,
            @Nonnull final Function<EntityType, SphereRequest<EntityType>> deleteFunction,
            @Nonnull String resourceName) {
        logger.debug("Cleanup {} table", resourceName);

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
