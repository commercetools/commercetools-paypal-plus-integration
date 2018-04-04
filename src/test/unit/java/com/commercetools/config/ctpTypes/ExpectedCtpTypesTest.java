package com.commercetools.config.ctpTypes;

import io.sphere.sdk.types.Type;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.io.File;
import java.util.Set;
import java.util.stream.Stream;

import static com.commercetools.config.ctpTypes.ExpectedCtpTypes.*;
import static io.sphere.sdk.json.SphereJsonUtils.readObjectFromResource;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.assertj.core.api.Assertions.assertThat;

public class ExpectedCtpTypesTest {

    /**
     * Verifies that {@link ExpectedCtpTypes} contains an set of available resources.
     */
    @Test
    public void getAllExpectedTypes_getResourcePath_convertedToValidResources() throws Exception {
        Stream.of(getAllExpectedTypes())
                .map(enumType -> Pair.of(enumType, readObjectFromResource(enumType.getResourcePath(), Type.class)))
                .forEach(typePair -> {
                    assertThat(typePair.getRight()).isNotNull();
                    // validate the enum keys are equal to respective Type#key
                    assertThat(typePair.getRight().getKey()).isEqualTo(typePair.getLeft().getKey());
                });
    }

    /**
     * Verify that all JSON resources in {@link ExpectedCtpTypes#PATH_TO_TYPE_MOCKS} are associated with some
     * {@link ExpectedCtpTypes} constant. If this test fails - adjust {@code ExpectedCtpTypes} constants set with
     * {@link ExpectedCtpTypes#PATH_TO_TYPE_MOCKS} resources directory content
     */
    @Test
    public void allResourcesInTheDirectory_haveMappedEnumConstants() throws Exception {
        File ctpTypesDir = new File(this.getClass().getClassLoader().getResource(PATH_TO_TYPE_MOCKS).toURI());
        assertThat(ctpTypesDir.exists()).isTrue();
        assertThat(ctpTypesDir.isDirectory()).isTrue();

        File[] ctpMockFiles = ctpTypesDir.listFiles(fileName -> fileName.getName().endsWith(CTP_TYPE_MOCK_EXTENSION));
        assertThat(ctpMockFiles).isNotEmpty();

        Set<String> fileNamesSet = Stream.of(ctpMockFiles).map(file -> getBaseName(file.getName())).collect(toSet());
        Set<String> typeKeysSet = getExpectedCtpTypesFromResources().stream().map(Type::getKey).collect(toSet());

        assertThat(fileNamesSet).isEqualTo(typeKeysSet);
    }
}