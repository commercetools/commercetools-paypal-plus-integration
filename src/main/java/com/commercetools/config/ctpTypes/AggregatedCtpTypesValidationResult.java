package com.commercetools.config.ctpTypes;

import io.sphere.sdk.types.Type;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.util.ArgumentsUtils.requireNonBlank;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Aggregated result of all tenants {@link Type} validations.
 * <p>
 * An instance could be:<ul>
 * <li>{@link #isEmpty()} - if no error messages of updated types</li>
 * <li>{@link #hasErrorMessage()} - if has error messages, hence no types should be updated</li>
 * <li>{@link #hasUpdatedTypes()} - no error messages, but some types were updated</li>
 * </ul>
 */
public class AggregatedCtpTypesValidationResult {

    @Nullable
    private final String aggregatedErrorMessage;

    /**
     * List of tuples of update types. Pair key(left) is a tenant name, value(right) - list of updated types on this
     * tenant.
     */
    @Nullable
    private final List<Pair<String, List<Type>>> updatedTypes;

    private AggregatedCtpTypesValidationResult(@Nullable String aggregatedErrorMessage,
                                               @Nullable List<Pair<String, List<Type>>> updatedTypes) {
        if (aggregatedErrorMessage != null && updatedTypes != null) {
            throw new IllegalArgumentException("For AggregatedCtpTypesValidationResult only one argument expected to be significant");
        }

        this.aggregatedErrorMessage = aggregatedErrorMessage;
        this.updatedTypes = updatedTypes;
    }

    public boolean hasErrorMessage() {
        return isNotBlank(aggregatedErrorMessage);
    }

    public boolean hasUpdatedTypes() {
        return isNotEmpty(updatedTypes);
    }

    public boolean isEmpty() {
        return !hasErrorMessage() && !hasUpdatedTypes();
    }

    @Nullable
    public String getAggregatedErrorMessage() {
        return aggregatedErrorMessage;
    }

    @Nonnull
    public List<Pair<String, List<Type>>> getUpdatedTypes() {
        return updatedTypes != null ? unmodifiableList(updatedTypes) : emptyList();
    }

    /**
     * @return aggregated result where neither error found, either types were updated
     */
    public static AggregatedCtpTypesValidationResult ofEmpty() {
        return new AggregatedCtpTypesValidationResult(null, null);
    }

    /**
     * @param aggregatedErrorMessage complete message to display to customer in the logs
     * @return instance with error message
     */
    public static AggregatedCtpTypesValidationResult ofErrorMessage(@Nonnull String aggregatedErrorMessage) {
        requireNonBlank(aggregatedErrorMessage, "aggregatedErrorMessage argument must be non-blank");
        return new AggregatedCtpTypesValidationResult(aggregatedErrorMessage, null);
    }

    /**
     * @param updatedTypes list of tuples, where pair key(left) is a tenant name, value(right) - list of updated types
     *                     on this tenant.
     * @return instance with update tenant-types pairs
     */
    public static AggregatedCtpTypesValidationResult ofUpdatedTypes(@Nonnull List<Pair<String, List<Type>>> updatedTypes) {
        return new AggregatedCtpTypesValidationResult(null, updatedTypes);
    }

    /**
     * From the list of all {@link TenantCtpTypesValidationAction}:<ul>
     * <li>if the {@code tenantTypeActions} list has at least one error: return error aggregated result</li>
     * <li>otherwise, if the {@code tenantTypeActions} list at least one update action: execute the actions and return
     * aggregated result with the list of {@link #updatedTypes}</li>
     * <li>otherwise return empty object</li>
     * </ul>
     *
     * @param tenantTypeActions list of tenant type actions to aggregate
     * @return completion stage with {@link AggregatedCtpTypesValidationResult} containing the result of the
     * errors/update actions executing and aggregating.
     */
    public static CompletionStage<AggregatedCtpTypesValidationResult> executeAndAggregateTenantValidationResults(
            @Nonnull List<TenantCtpTypesValidationAction> tenantTypeActions) {

        List<TenantCtpTypesValidationAction> typeErrors = tenantTypeActions.stream()
                .filter(TenantCtpTypesValidationAction::hasErrors)
                .collect(toList());

        // 1. If some errors exist - return error result
        if (typeErrors.size() > 0) {
            return completedFuture(
                    ofErrorMessage(format("CTP Types configuration exception:%n%s%n",
                            typeErrors.stream()
                                    .flatMap(validationError -> validationError.getErrorMessages().stream())
                                    .map(errorMessage -> "    " + errorMessage)
                                    .collect(joining(format("%n"))))));

        }

        List<TenantCtpTypesValidationAction> typeUpdates = tenantTypeActions.stream()
                .filter(TenantCtpTypesValidationAction::hasExecuteActions)
                .collect(toList());

        // 2. If update actions exist - return list of updated types
        if (typeUpdates.size() > 0) {
            List<CompletableFuture<Pair<String, List<Type>>>> typeFutures =
                    typeUpdates.stream()
                            .map(TenantCtpTypesValidationAction::executeActions)
                            .map(CompletionStage::toCompletableFuture)
                            .collect(toList());

            return allOf(typeFutures.toArray(new CompletableFuture[]{}))
                    .thenApply(ignoreVoid -> typeFutures.stream()
                            .map(CompletableFuture::join)
                            .collect(toList()))
                    .thenApply(AggregatedCtpTypesValidationResult::ofUpdatedTypes);
        }

        // 3. If nothing to update - return empty
        return completedFuture(ofEmpty());
    }
}
