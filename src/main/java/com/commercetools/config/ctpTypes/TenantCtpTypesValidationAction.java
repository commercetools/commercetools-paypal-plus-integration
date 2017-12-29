package com.commercetools.config.ctpTypes;

import com.commercetools.service.ctp.TypeService;
import io.sphere.sdk.types.FieldDefinition;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.TypeDraftBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Object to reflect and execute (if necessary) certain tenant validation action.
 * <ul>
 * <li>If the instance {@link #hasErrors() == true} - all action should be skipped.</li>
 * <li>If the instance {@link #hasExecuteActions()} == true - the actions should be performed to synchronized types</li>
 * <li>If both of above are false - the action should be skipped.</li>
 * </ul>
 */
public class TenantCtpTypesValidationAction {

    private final String tenantName;

    private final TypeService typeService;

    private final List<Type> typesToCreate = new ArrayList<>();
    private final List<Pair<Type, List<FieldDefinition>>> fieldDefinitionsToAdd = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    private TenantCtpTypesValidationAction(@Nonnull String tenantName, @Nonnull TypeService typeService) {
        this.tenantName = tenantName;
        this.typeService = typeService;
    }

    private TenantCtpTypesValidationAction(@Nonnull String tenantName, @Nonnull TypeService typeService,
                                           @Nonnull List<String> errorMessages) {
        this(tenantName, typeService);
        this.errorMessages.addAll(errorMessages);
    }

    /**
     * @return <b>true</b> if at least one error message exists.
     */
    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    /**
     * @return <b>true</b> if there are some types to create or update.
     */
    public boolean hasExecuteActions() {
        return isNotEmpty(typesToCreate) || isNotEmpty(fieldDefinitionsToAdd);
    }

    public List<String> getErrorMessages() {
        return unmodifiableList(errorMessages);
    }

    /**
     * Perform create/addField actions on current {@link #typeService}. It is expected that the types are different
     * in the actions lists (the type either completely created from scratch or the fields are added to the one existing
     * type associated in {@link #fieldDefinitionsToAdd} pair), but both actions are impossible. Hence the operation
     * are performed in parallel using default fork join pool for streams.
     *
     * @return completion stage of updated types, aggregated to pairs of {@code tenantName -> List of updatedTypes}
     */
    public CompletionStage<Pair<String, List<Type>>> executeActions() {
        Stream<CompletionStage<Type>> createTypesStream = typesToCreate.stream().map(type ->
                TypeDraftBuilder
                        .of(type.getKey(), type.getName(), type.getResourceTypeIds())
                        .name(type.getName())
                        .description(type.getDescription())
                        .fieldDefinitions(type.getFieldDefinitions())
                        .build())
                .map(typeService::createType);

        Stream<CompletionStage<Type>> addFieldsStream =
                fieldDefinitionsToAdd.stream()
                        .map(pair -> typeService.addFieldDefinitions(pair.getKey(), pair.getValue()));

        List<CompletableFuture<Type>> allUpdates = Stream.concat(createTypesStream, addFieldsStream)
                .map(CompletionStage::toCompletableFuture)
                .collect(toList());

        return allOf(allUpdates.toArray(new CompletableFuture[]{}))
                .thenApply(ignoreVoid -> allUpdates.stream()
                        .map(CompletableFuture::join)
                        .collect(toList()))
                .thenApply(listOfUpdatedTypes -> Pair.of(tenantName, listOfUpdatedTypes));
    }

    /**
     * Create empty object: no errors or actions are set in this instance.
     *
     * @param tenantName  tenant name on which to create a new type
     * @param typeService {@link TypeService} on which to create the types. The type service is expected to be associated
     *                    with respective {@code tenantName}
     */
    public static TenantCtpTypesValidationAction ofEmpty(@Nonnull String tenantName, @Nonnull TypeService typeService) {
        return new TenantCtpTypesValidationAction(tenantName, typeService);
    }

    /**
     * Create Type action.
     *
     * @param tenantName  tenant name on which to create a new type
     * @param typeService {@link TypeService} on which to create the types. The type service is expected to be associated
     *                    with respective {@code tenantName}
     * @param type        {@link Type} to create
     * @return instance with create type action
     */
    public static TenantCtpTypesValidationAction ofCreateType(@Nonnull String tenantName, @Nonnull TypeService typeService,
                                                              @Nonnull Type type) {
        TenantCtpTypesValidationAction res = ofEmpty(tenantName, typeService);
        res.typesToCreate.add(type);
        return res;
    }

    /**
     * Add field definitions to the actual type.
     *
     * @param tenantName       tenant name on which to create a new type
     * @param typeService      {@link TypeService} on which to create the types. The type service is expected to be associated
     *                         with respective {@code tenantName}
     * @param actualType       {@link Type} to be updated.
     * @param fieldDefinitions list of field definitions to add. If the list is empty -
     *                         empty {@link TenantCtpTypesValidationAction} is returned.
     * @return action instance with add field definition actions if {@code fieldDefinitions} has values, otherwise -
     * empty object.
     */
    public static TenantCtpTypesValidationAction ofAddFieldDefinitions(@Nonnull String tenantName,
                                                                       @Nonnull TypeService typeService,
                                                                       @Nonnull Type actualType,
                                                                       @Nonnull List<FieldDefinition> fieldDefinitions) {
        TenantCtpTypesValidationAction res = ofEmpty(tenantName, typeService);

        if (isNotEmpty(fieldDefinitions)) {
            res.fieldDefinitionsToAdd.add(Pair.of(actualType, fieldDefinitions));
        }

        return res;
    }

    /**
     * Create action instance with error messages only.
     *
     * @param tenantName    tenant name on which to create a new type
     * @param typeService   {@link TypeService} on which to create the types. The value will be ignored since no
     *                      operation should be performed in case of error.
     * @param errorMessages list of error message to display to customers
     * @return action instance with error messages.
     */
    public static TenantCtpTypesValidationAction ofErrorMessages(@Nonnull String tenantName, @Nonnull TypeService typeService,
                                                                 @Nonnull List<String> errorMessages) {
        return new TenantCtpTypesValidationAction(tenantName, typeService, errorMessages);
    }

    /**
     * Aggregate/merge actions to single action. This aggregated value should be later verified and:
     * <ul>
     * <li>If has error messages - display then and close the application</li>
     * <li>Otherwise, if has actions to perform - perform them (create/update types)</li>
     * <li>Otherwise (if empty) - ignore</li>
     * </ul>
     *
     * @param val1 {@link TenantCtpTypesValidationAction} to merge
     * @param val2 {@link TenantCtpTypesValidationAction} to merge
     * @return aggregated instance, which contains all error messages and all create/add actions from {@code val1} and
     * {@code val2}.
     */
    static TenantCtpTypesValidationAction merge(@Nonnull TenantCtpTypesValidationAction val1,
                                                @Nonnull TenantCtpTypesValidationAction val2) {
        if (!Objects.equals(val1.tenantName, val2.tenantName)) {
            throw new IllegalArgumentException("TenantCtpTypesValidationAction#merge() expected to merge actions for the same tenant config");
        }

        TenantCtpTypesValidationAction res = ofEmpty(val1.tenantName, val1.typeService);

        res.errorMessages.addAll(val1.errorMessages);
        res.errorMessages.addAll(val2.errorMessages);
        res.typesToCreate.addAll(val1.typesToCreate);
        res.typesToCreate.addAll(val2.typesToCreate);
        res.fieldDefinitionsToAdd.addAll(val1.fieldDefinitionsToAdd);
        res.fieldDefinitionsToAdd.addAll(val2.fieldDefinitionsToAdd);

        return res;

    }
}
