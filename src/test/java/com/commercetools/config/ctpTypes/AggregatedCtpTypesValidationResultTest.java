package com.commercetools.config.ctpTypes;

import com.commercetools.service.ctp.impl.TypeServiceImpl;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.types.FieldDefinition;
import io.sphere.sdk.types.StringFieldType;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.TypeDraft;
import io.sphere.sdk.types.commands.updateactions.AddFieldDefinition;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.commercetools.config.ctpTypes.AggregatedCtpTypesValidationResultTest.TypeServiceMock.typeServiceMock;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AggregatedCtpTypesValidationResultTest {

    @Test
    public void ofEmpty() throws Exception {
        AggregatedCtpTypesValidationResult result = AggregatedCtpTypesValidationResult.ofEmpty();
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.hasErrorMessage()).isFalse();
        assertThat(result.getAggregatedErrorMessage()).isEmpty();
        assertThat(result.hasUpdatedTypes()).isFalse();
        assertThat(result.getUpdatedTypes()).isEmpty();
    }

    @Test
    public void ofErrorMessage() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> AggregatedCtpTypesValidationResult.ofErrorMessage(""));
        assertThatIllegalArgumentException().isThrownBy(() -> AggregatedCtpTypesValidationResult.ofErrorMessage("   "));

        AggregatedCtpTypesValidationResult result = AggregatedCtpTypesValidationResult.ofErrorMessage("It's amazing how much \"mature wisdom\" resembles being too tired");
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.hasErrorMessage()).isTrue();
        assertThat(result.getAggregatedErrorMessage()).isEqualTo("It's amazing how much \"mature wisdom\" resembles being too tired");
        assertThat(result.hasUpdatedTypes()).isFalse();
        assertThat(result.getUpdatedTypes()).isEmpty();
    }

    @Test
    public void ofUpdatedTypes() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> AggregatedCtpTypesValidationResult.ofUpdatedTypes(emptyList()));

        AggregatedCtpTypesValidationResult result = AggregatedCtpTypesValidationResult.ofUpdatedTypes(asList(
                Pair.of("tenant1", asList(mockType("t1_key1"), mockType("t1_key2"))),
                Pair.of("tenant2", singletonList(mockType("t2_key1")))
        ));
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.hasErrorMessage()).isFalse();
        assertThat(result.getAggregatedErrorMessage()).isEmpty();
        assertThat(result.hasUpdatedTypes()).isTrue();

        String concatTenantNamesTypeKeys = concatUpdatedTypesToString(result);
        assertThat(concatTenantNamesTypeKeys).isEqualTo("tenant1#t1_key1,t1_key2;tenant2#t2_key1");
    }

    @Test
    public void executeAndAggregateTenantValidationResults_withEmptyActions_shouldReturnEmptyResult() throws Exception {
        AggregatedCtpTypesValidationResult result = AggregatedCtpTypesValidationResult.executeAndAggregateTenantValidationResults(asList(
                TenantCtpTypesValidationAction.ofEmpty("tenant1", typeServiceMock()),
                TenantCtpTypesValidationAction.ofEmpty("tenant2", typeServiceMock())
        )).toCompletableFuture().join();

        assertThat(result).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.hasErrorMessage()).isFalse();
        assertThat(result.getAggregatedErrorMessage()).isEmpty();
        assertThat(result.hasUpdatedTypes()).isFalse();
        assertThat(result.getUpdatedTypes()).isEmpty();
    }

    @Test
    public void executeAndAggregateTenantValidationResults_withErrorAction_shouldReturnErrorResult() throws Exception {
        AggregatedCtpTypesValidationResult result = AggregatedCtpTypesValidationResult.executeAndAggregateTenantValidationResults(asList(
                TenantCtpTypesValidationAction.ofEmpty("tenant1", typeServiceMock()),
                TenantCtpTypesValidationAction.ofCreateType("tenant2", typeServiceMock(), mockType("t2_key1")),
                TenantCtpTypesValidationAction.ofUpdateActions("tenant3", typeServiceMock(), mockType("t3_key1"),
                        singletonList(AddFieldDefinition.of(FieldDefinition.of(StringFieldType.of(), "field1", LocalizedString.of(), false)))),
                TenantCtpTypesValidationAction.ofErrorMessages("tenant3", typeServiceMock(), asList("error1", "error2"))
        )).toCompletableFuture().join();

        assertThat(result).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.hasErrorMessage()).isTrue();
        assertThat(result.getAggregatedErrorMessage()).contains("error1", "error2");
        assertThat(result.hasUpdatedTypes()).isFalse();
        assertThat(result.getUpdatedTypes()).isEmpty();
    }

    @Test
    public void executeAndAggregateTenantValidationResults_withUpdateActions_shouldReturnUpdatedTypesList() throws Exception {
        AggregatedCtpTypesValidationResult result = AggregatedCtpTypesValidationResult.executeAndAggregateTenantValidationResults(asList(
                TenantCtpTypesValidationAction.ofEmpty("tenant1", typeServiceMock()),
                TenantCtpTypesValidationAction.ofCreateType("tenant2", typeServiceMock(), mockType("t2_key1")),
                TenantCtpTypesValidationAction.ofUpdateActions("tenant3", typeServiceMock(), mockType("t3_key1"),
                        singletonList(AddFieldDefinition.of(FieldDefinition.of(StringFieldType.of(), "field1", LocalizedString.of(), false)))),
                TenantCtpTypesValidationAction.ofEmpty("tenant4", typeServiceMock())
        )).toCompletableFuture().join();

        assertThat(result).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isFalse();
        assertThat(result.hasErrorMessage()).isFalse();
        assertThat(result.getAggregatedErrorMessage()).isEmpty();
        assertThat(result.hasUpdatedTypes()).isTrue();
        assertThat(concatUpdatedTypesToString(result)).isEqualTo("tenant2#t2_key1;tenant3#t3_key1");
    }

    /**
     * From update types list produce a string in format:
     * {@code tenantName1#typeKey1,typeKey2;tenantName1#typeKey3,typeKey4......}
     */
    private static String concatUpdatedTypesToString(AggregatedCtpTypesValidationResult aggregatedResult) {
        return aggregatedResult.getUpdatedTypes().stream()
                .map(pair -> format("%s#%s", pair.getLeft(), pair.getRight().stream().map(Type::getKey).collect(joining(","))))
                .collect(joining(";"));
    }

    private static Type mockType(String key) {
        Type type = mock(Type.class);
        when(type.getKey()).thenReturn(key);

        return type;
    }

    static class TypeServiceMock extends TypeServiceImpl {
        TypeServiceMock() {
            super(mock(SphereClient.class));
        }

        @Override
        public CompletionStage<Type> createType(@Nonnull TypeDraft typeDraft) {
            return completedFuture(mockType(typeDraft.getKey()));
        }

        @Override
        public CompletionStage<Type> updateType(@Nonnull Type type, @Nonnull List<UpdateAction<Type>> updateActions) {
            return completedFuture(type);
        }

        static TypeServiceMock typeServiceMock() {
            return new TypeServiceMock();
        }
    }
}