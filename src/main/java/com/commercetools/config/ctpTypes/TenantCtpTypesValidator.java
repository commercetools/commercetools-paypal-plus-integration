package com.commercetools.config.ctpTypes;

import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.ctp.TypeService;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.WithKey;
import io.sphere.sdk.types.*;
import io.sphere.sdk.types.commands.updateactions.AddEnumValue;
import io.sphere.sdk.types.commands.updateactions.AddFieldDefinition;
import io.sphere.sdk.types.commands.updateactions.AddLocalizedEnumValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.commercetools.config.ctpTypes.TenantCtpTypesValidationAction.*;
import static io.sphere.sdk.utils.CompletableFutureUtils.listOfFuturesToFutureOfList;
import static java.lang.String.format;
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
     * For every tenant get a list of errors or update actions and execute them.
     * Return aggregated validation/syncing result for all tenants and types.
     *
     * @return stage of {@link AggregatedCtpTypesValidationResult} with types validation/synchronization results
     * for all tenants.
     */
    public static CompletionStage<AggregatedCtpTypesValidationResult> validateAndSyncCtpTypes(
            @Nonnull CtpFacadeFactory ctpFacadeFactory,
            @Nonnull List<TenantConfig> tenantConfigs,
            @Nonnull Set<Type> expectedTypesSet) {

        // 1. fetch and validate expected types from all the tenants
        List<CompletableFuture<TenantCtpTypesValidationAction>> tenantsValidatorsFutures =
                tenantConfigs.parallelStream()
                        .map(tenantConfig -> TenantCtpTypesValidator.validateTenantTypes(tenantConfig.getTenantName(),
                                ctpFacadeFactory.getCtpFacade(tenantConfig).getTypeService(),
                                expectedTypesSet))
                        .map(CompletionStage::toCompletableFuture)
                        .collect(toList());

        // 2. process/aggregate validation results
        return listOfFuturesToFutureOfList(tenantsValidatorsFutures)
                .thenCompose(AggregatedCtpTypesValidationResult::executeAndAggregateTenantValidationResults);
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
        // 3. If one of fieldDefinition is incompatible with actual type create error message (see #getFieldsMismatchMessages() for details)
        // 4. If one of the expectedType fields, enum/lenum keys are missing - update the actual types

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

        List<UpdateAction<Type>> typeUpdateActions = Stream.of(
                addFieldDefinition(expectedType, actualType),
                addEnumValues(expectedType, actualType),
                addLocalizedEnumValues(expectedType, actualType))
                .flatMap(i -> i)
                .collect(toList());

        // 4. create missing field definitions, enum/lenum keys
        return ofUpdateActions(tenantName, typeService, actualType, typeUpdateActions);
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
     * if some field definitions are mismatched and can't be updated.
     *
     * @param tenantName   tenant name to display in the error message
     * @param expectedType expected type
     * @param actualType   actual type
     * @return stream of error messages, if some filed definitions are mismatched in the {@code actualType}
     * and can't be updated. If all actual field definitions are valid - return empty stream.
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
     * are different. <i>Different</i> means at least one of:<ul>
     * <li>they have mismatch in {@code name}, or {@code type} or {@code required} properties.</li>
     * <li>if field type is {@link SetFieldType} - {io.sphere.sdk.types.SetFieldType#getElementType() SetFieldType#getElementType() is not the same</li>
     * <li>if field type is {@link ReferenceFieldType} - {io.sphere.sdk.types.ReferenceFieldType#getReferenceTypeId() ReferenceFieldType#getReferenceTypeId() is not the same</li>
     * </ul>
     * See {@link FieldDefinitionTuple#areDifferent()} for more details.
     * <p>
     * If field in {@code actualType} is missing - skip it (it should be added by {@link #addFieldDefinition(Type, Type)})
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
    private static Stream<AddFieldDefinition> addFieldDefinition(@Nonnull Type expectedType, @Nonnull Type actualType) {
        return expectedType.getFieldDefinitions().stream()
                .filter(expectedField -> actualType.getFieldDefinitionByName(expectedField.getName()) == null)
                .map(AddFieldDefinition::of);
    }

    @Nonnull
    private static Stream<AddEnumValue> addEnumValues(@Nonnull Type expectedType, @Nonnull Type actualType) {
        return addTypedEnumValues(expectedType, actualType,
                EnumFieldType.class,
                EnumFieldType::getValues,
                AddEnumValue::of);
    }

    @Nonnull
    private static Stream<AddLocalizedEnumValue> addLocalizedEnumValues(@Nonnull Type expectedType, @Nonnull Type actualType) {
        return addTypedEnumValues(expectedType, actualType,
                LocalizedEnumFieldType.class,
                LocalizedEnumFieldType::getValues,
                AddLocalizedEnumValue::of);
    }

    /**
     * Function to compare and add missing enum/lenum keys. If some values are redundant in the actual field definition
     * - they are not removed.
     *
     * @param expectedType       expected type
     * @param actualType         actual type
     * @param clazz              actual {@link EnumFieldType} or {@link LocalizedEnumFieldType} class
     * @param getValues          function to get values list from enum/lenum
     * @param enumToActionMapper function to map missing enum/value to type update action
     * @param <FT>               field type ({@link EnumFieldType} or {@link LocalizedEnumFieldType})
     * @param <FTC>              field type class ({@code EnumFieldType.class} or {@code LocalizedEnumFieldType.class})
     * @param <ET>               type of enum/lenum values (type of {@code #getValues()} list entires)
     * @param <UAT>              actual {@link UpdateAction} type
     * @return stream of update actions, if required. Empty stream if no expected values should be added.
     */
    @Nonnull
    private static <FT extends FieldType, FTC extends Class<FT>, ET extends WithKey, UAT extends UpdateAction<Type>>
    Stream<UAT> addTypedEnumValues(
            @Nonnull Type expectedType, @Nonnull Type actualType,
            @Nonnull FTC clazz,
            @Nonnull Function<FT, List<ET>> getValues,
            @Nonnull BiFunction<String, ET, UAT> enumToActionMapper) {
        return expectedType.getFieldDefinitions().stream()
                .flatMap(expectedDefinition -> {
                    FieldDefinition actualDefinition = actualType.getFieldDefinitionByName(expectedDefinition.getName());
                    if (actualDefinition != null) {
                        FT actualFieldType = safeCastInstanceToType(clazz, actualDefinition.getType());
                        FT expectedFieldType = safeCastInstanceToType(clazz, expectedDefinition.getType());
                        if (actualFieldType != null && expectedFieldType != null) {
                            Set<ET> actualEnumValues = new HashSet<>(getValues.apply(actualFieldType));
                            List<ET> expectedEnumValues = getValues.apply(expectedFieldType);
                            return expectedEnumValues.stream()
                                    .filter(enumValue -> !actualEnumValues.contains(enumValue))
                                    .map(enumValue -> enumToActionMapper.apply(actualDefinition.getName(), enumValue));
                        }
                    }
                    return Stream.empty();
                });
    }

    /**
     * @param clazz class to which to cast
     * @param o     instance to try to cast
     * @param <T>   type of instance we would like to cast in
     * @return casted instance, if possible, <b>null</b> otherwise.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T safeCastInstanceToType(@Nonnull Class<T> clazz, @Nullable Object o) {
        return clazz.isInstance(o) ? (T) o : null;
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
         * <li>for {@link ReferenceFieldType} - different {@link ReferenceFieldType#referenceTypeId¬}</li>
         * <li>for {@link SetFieldType} - different {@link SetFieldType#elementType}</li>
         * </ul>
         * <p>
         * <b>Note:</b> {@link EnumFieldType} and {@link LocalizedEnumFieldType} are expected to be equal
         * (e.g. <b>not</b> different) even if they have different values.
         * The missed values should be added in update actions.
         */
        public boolean areDifferent() {
            return (expectedField != actualField)
                    && !(haveSameCommonValues() && haveSameFieldTypeDeclaration());
        }

        private boolean haveSameCommonValues() {
            return Objects.equals(expectedField.getName(), actualField.getName())
                    && Objects.equals(expectedField.isRequired(), actualField.isRequired());
        }

        private boolean haveSameFieldTypeDeclaration() {
            FieldType expectedFieldType = expectedField.getType();
            FieldType actualFieldType = actualField.getType();

            // Note: comparing FieldType by FieldType#equals() doesn't satisfy our needs,
            // because enum/lenum types compare also values, but values is a kind of "recoverable" mismatch
            // and the Type#fieldDefinition should be updated adding missing values.
            if (expectedFieldType instanceof EnumFieldType || expectedFieldType instanceof LocalizedEnumFieldType) {
                return Objects.equals(expectedFieldType.getClass(), actualFieldType.getClass());
            }

            // all other field types must be completely equal
            return Objects.equals(expectedFieldType, actualFieldType);
        }
    }

}
