package com.commercetools.config.ctpTypes;

import io.sphere.sdk.types.Type;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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

        String concatTenantNamesTypeKeys = result.getUpdatedTypes().stream()
                .map(pair -> format("%s#%s", pair.getLeft(), pair.getRight().stream().map(Type::getKey).collect(joining(","))))
                .collect(joining(";"));
        assertThat(concatTenantNamesTypeKeys).isEqualTo("tenant1#t1_key1,t1_key2;tenant2#t2_key1");
    }

    private static Type mockType(String key) {
        Type type = mock(Type.class);
        when(type.getKey()).thenReturn(key);

        return type;
    }
}