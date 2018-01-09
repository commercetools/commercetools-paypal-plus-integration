package com.commercetools.config.ctpTypes;

import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.ctp.TypeService;
import io.sphere.sdk.types.FieldDefinition;
import io.sphere.sdk.types.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static com.commercetools.config.ctpTypes.TenantCtpTypesValidationAction.*;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This class performs {@link Type} validation and produces {@link TenantCtpTypesValidationAction} for each tenant.
 * See {@link #validateTenantTypes(String, TypeService, Set)} for more details
 */
public class TenantCtpTypesValidator {

    /**
     * Tenant to validate
     */
    @Nonnull
    private final String tenantName;

    /**
     * {@link TypeService} instance on which perform validation (read and update types)
     */
    @Nonnull
    private final TypeService typeService;

    /**
     * Set of expected CTP types on the tenant's project.
     */
    @Nonnull
    private final Set<Type> expectedTypesSet;


    TenantCtpTypesValidator(@Nonnull String tenantName,
                            @Nonnull TypeService typeService,
                            @Nonnull Set<Type> expectedTypesSet) {

        this.tenantName = tenantName;
        this.typeService = typeService;
        this.expectedTypesSet = expectedTypesSet;
    }

    /**
     * For every tenant get a list of errors or update actions and execute them. Return aggregated result.
     *
     * @return {@link AggregatedCtpTypesValidationResult} with types validation/synchronization results for all tenants.
     */
    public static AggregatedCtpTypesValidationResult getAggregatedCtpTypesValidationResult(
            @Nonnull CtpFacadeFactory ctpFacadeFactory,
            @Nonnull List<TenantConfig> tenantConfigs,
            @Nonnull Set<Type> expectedTypesSet) {

        // 1. fetch and validate expected types from all the tenants
        List<CompletableFuture<TenantCtpTypesValidationAction>> tenantsValidatorsStage =
                tenantConfigs.parallelStream()
                        .map(tenantConfig -> TenantCtpTypesValidator.validateTenantTypes(tenantConfig.getTenantName(),
                                ctpFacadeFactory.getCtpFacade(tenantConfig).getTypeService(),
                                expectedTypesSet))
                        .map(CompletionStage::toCompletableFuture)
                        .collect(toList());

        // 2. process/aggregate validation results
        return allOf(tenantsValidatorsStage.toArray(new CompletableFuture[]{}))
                .thenApply(voidValue -> tenantsValidatorsStage.stream().map(CompletableFuture::join).collect(toList()))
                .thenCompose(AggregatedCtpTypesValidationResult::executeAndAggregateTenantValidationResults)
                .join();
    }

    /**
     * Fetch tenants {@link Type}s set from the project, compare it with {@code expectedTypesSet} and return
     * stage of {@link TenantCtpTypesValidationAction} instance based on the comparing results.
     *
     * @param tenantName       tenant to validate/update
     * @param typeService      {@link TypeService} instance which is used to read/update types
     * @param expectedTypesSet set of expected CTP type which is used to compare with actual project types
     * @return completion stage of {@link TenantCtpTypesValidationAction}, which might be either (if all the types match),
     * or contain CTP type update actions, or contain error message if the types can't be synchronized.
     */
    private static CompletionStage<TenantCtpTypesValidationAction> validateTenantTypes(@Nonnull String tenantName,
                                                                                       @Nonnull TypeService typeService,
                                                                                       @Nonnull Set<Type> expectedTypesSet) {
        TenantCtpTypesValidator validator = new TenantCtpTypesValidator(tenantName, typeService, expectedTypesSet);
        return validator.typeService.getTypes()
                .thenApply(actualTypesList -> actualTypesList.stream().collect(toMap(Type::getKey, i -> i)))
                .thenApply(validator::validateExpectedAndActualTypes);
    }

    /**
     * Compare {@link #expectedTypesSet} with {@code actualCtpTypes} and product validation action.
     *
     * @param actualCtpTypes fetched from {@link #typeService} CTP types associated with current {@link #tenantName}
     * @return {@link TenantCtpTypesValidationAction} instance based on types comparing.
     */
    private TenantCtpTypesValidationAction validateExpectedAndActualTypes(@Nonnull Map<String, Type> actualCtpTypes) {
        return expectedTypesSet.stream()
                .map(expectedType -> mapExpectedVsActualToAction(expectedType, actualCtpTypes.get(expectedType.getKey())))
                .reduce(TenantCtpTypesValidationAction::merge)
                .orElseGet(() -> TenantCtpTypesValidationAction.ofEmpty(tenantName, typeService));
    }

    /**
     * Create {@link TenantCtpTypesValidationAction} instance based on {@code expectedType} and {@code actualType}
     * comparing.
     *
     * @param expectedType expected type to compare.
     * @param actualType   actual type to compare. If {@code null} - returned action contains create type action.
     * @return instance of {@link TenantCtpTypesValidationAction} reflecting the types comparing.
     */
    private TenantCtpTypesValidationAction mapExpectedVsActualToAction(final @Nonnull Type expectedType, final @Nullable Type actualType) {
        // 1. If actualType does not exist - create a new type
        // 2. If actualType does not contain all expected resourceTypeId - create error message
        // 3. If one of fieldDefinition has "type" or "required" field mismatch - create error message
        // 4. If one of the expectedType fields is missing - update the actual type adding this messages

        // 1. create a new type
        if (actualType == null) {
            return ofCreateType(tenantName, typeService, expectedType);
        }

        // 2. collect missing resource type ids
        // 3. collect fields mismatches
        List<String> errorMessages = Stream.concat(
                getMissingResourceTypeMessages(tenantName, expectedType, actualType),
                getFieldsMismatchMessages(tenantName, expectedType, actualType))
                .collect(toList());

        if (!errorMessages.isEmpty()) {
            return ofErrorMessages(tenantName, typeService, errorMessages);
        }

        // 4. create missing field definitions, if any
        return ofAddFieldDefinitions(tenantName, typeService,
                actualType,
                getMissingFields(expectedType, actualType)
                        .collect(toList()));
    }

    /**
     * Compare {@link Type#getResourceTypeIds()} sets and return stream of error messages is some resource type ids are
     * missing in actual type
     *
     * @param tenantName   tenant name to display in the error message
     * @param expectedType expected type
     * @param actualType   actual type
     * @return stream of error messages, if some ids are missing in the {@code actualType}. If no id is missing -
     * return empty stream.
     */
    private static Stream<String> getMissingResourceTypeMessages(final @Nonnull String tenantName,
                                                                 final @Nonnull Type expectedType, final @Nonnull Type actualType) {
        return getMissingResourceTypes(expectedType, actualType)
                .map(expectedResourceTypeId -> format("Expected Resource Type Id [%s] is missing for [%s]:Types:[%s]",
                        expectedResourceTypeId, tenantName, expectedType.getKey()));
    }

    /**
     * Compare {@code expectedType} with {@code actualType} fields definitions and return a stream of error messages,
     * if some field definitions are mismatched.
     *
     * @param tenantName   tenant name to display in the error message
     * @param expectedType expected type
     * @param actualType   actual type
     * @return stream of error messages, if some filed definitions are mismatched in the {@code actualType}.
     * If all actual field definitions are valid - return empty stream.
     */
    private static Stream<String> getFieldsMismatchMessages(final @Nonnull String tenantName,
                                                            final @Nonnull Type expectedType, final @Nonnull Type actualType) {
        return getMismatchedFields(expectedType, actualType)
                .map(mismatchedTuple ->
                        format("Field definition [%s]:Types:[%s]:[%s] error:%n" +
                                        "    expected type=[%s], required=[%b],%n" +
                                        "    actual   type=[%s], required=[%b]%n",
                                tenantName, expectedType.getKey(), mismatchedTuple.expectedField.getName(),
                                mismatchedTuple.expectedField.getType(), mismatchedTuple.expectedField.isRequired(),
                                mismatchedTuple.actualField.getType(), mismatchedTuple.actualField.isRequired())
                );
    }

    /**
     * @param expectedType expected type
     * @param actualType   actual type
     * @return stream of resource type ids, which exist in {@code expectedType}, but are missing in {@code actualType}.
     * If all fields are matching each other - return empty stream. <b>Note:</b> redundant resources ids in
     * {@code actualType} are treated as a valid case, e.g. they are not compared at all.
     */
    private static Stream<String> getMissingResourceTypes(final @Nonnull Type expectedType, final @Nonnull Type actualType) {
        return expectedType.getResourceTypeIds().stream()
                .filter(expectedResourceTypeId -> !actualType.getResourceTypeIds().contains(expectedResourceTypeId));
    }


    /**
     * Compare field definitions of expected and actual types. Returns a stream of {@link FieldDefinitionTuple} which
     * are different. <i>Different</i> means they have mismatch in {@code name}, {@code type} or {@code required} properties.
     * See {@link FieldDefinitionTuple#areDifferent()} for more details.
     * <p>
     * If field in {@code actualType} is missing - skip it (it should be added by {@link #getMissingFields(Type, Type)})
     *
     * @param expectedType expected type
     * @param actualType   actual type
     * @return stream of field {@link FieldDefinitionTuple} which are different in in {@code actualType}.
     * @see FieldDefinitionTuple#of(FieldDefinition, FieldDefinition)
     * @see FieldDefinitionTuple#areDifferent()
     */
    private static Stream<FieldDefinitionTuple> getMismatchedFields(final @Nonnull Type expectedType, final @Nonnull Type actualType) {
        return expectedType.getFieldDefinitions().stream()
                .map(expectedField -> FieldDefinitionTuple.of(expectedField, actualType.getFieldDefinitionByName(expectedField.getName())))
                .filter(Objects::nonNull)
                .filter(FieldDefinitionTuple::areDifferent);
    }

    /**
     * @param expectedType expected type
     * @param actualType   actual type
     * @return stream of fields which are completely missing in {@code actualType}, or empty stream, if
     * {@code actualType} contains all expected fields.
     */
    private static Stream<FieldDefinition> getMissingFields(final @Nonnull Type expectedType, final @Nonnull Type actualType) {
        return expectedType.getFieldDefinitions().stream()
                .filter(expectedField -> actualType.getFieldDefinitionByName(expectedField.getName()) == null);
    }

    /**
     * Pair of {@code expectedField/actualFiled} to compare them.
     */
    public static class FieldDefinitionTuple {
        @Nonnull
        private final FieldDefinition expectedField;

        @Nonnull
        private final FieldDefinition actualField;

        private FieldDefinitionTuple(@Nonnull FieldDefinition expectedField, @Nonnull FieldDefinition actualField) {
            this.expectedField = expectedField;
            this.actualField = actualField;
        }

        /**
         * @param expectedField real expected field
         * @param actualField   actual field to compare. If {@code null} - null is returned (e.g. nothing to compare)
         * @return {@link FieldDefinitionTuple} if actual field exists, {@code null} otherwise.
         */
        @Nullable
        public static FieldDefinitionTuple of(@Nonnull FieldDefinition expectedField, @Nullable FieldDefinition actualField) {
            return actualField != null
                    ? new FieldDefinitionTuple(expectedField, actualField)
                    : null;
        }

        /**
         * @return <b>true</b> if {@link #expectedField} and {@link #actualField} have different at least one of:
         * <ul>
         * <li>{@link FieldDefinition#getName()}</li>
         * <li>{@link FieldDefinition#getType()}</li>
         * <li>{@link FieldDefinition#isRequired()}</li>
         * </ul>
         */
        public boolean areDifferent() {
            return (expectedField != actualField)
                    && !(Objects.equals(expectedField.getName(), actualField.getName())
                    && Objects.equals(expectedField.getType(), actualField.getType())
                    && Objects.equals(expectedField.isRequired(), actualField.isRequired()));
        }
    }

}
