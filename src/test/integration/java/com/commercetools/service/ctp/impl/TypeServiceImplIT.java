package com.commercetools.service.ctp.impl;

import com.commercetools.Application;
import com.commercetools.service.ctp.TypeService;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.types.*;
import io.sphere.sdk.types.commands.updateactions.ChangeKey;
import io.sphere.sdk.types.commands.updateactions.ChangeName;
import org.bitbucket.radistao.test.annotation.BeforeAllMethods;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupOrdersCartsPayments;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupTypes;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Locale.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BeforeAfterSpringTestRunner.class)
@SpringBootTest(classes = Application.class)
public class TypeServiceImplIT {

    @Autowired
    private SphereClient sphereClient;

    @Autowired
    private TypeService typeService;

    /**
     * Since types could be bound to some orders/carts/payments - clean them first.
     * The actual types cleaning will be performed before/after each test.
     */
    @BeforeAllMethods
    public void setupBeforeAll() {
        cleanupOrdersCartsPayments(sphereClient);
    }

    @Before
    public void setUp() throws Exception {
        cleanupTypes(sphereClient);
    }

    @After
    public void tearDown() throws Exception {
        cleanupTypes(sphereClient);
    }

    @Test
    public void getAndCreateTypes() throws Exception {
        List<Type> types = executeBlocking(typeService.getTypes());
        assertThat(types).isEmpty();

        executeBlocking(typeService.createType(TypeDraftBuilder
                .of("test-type-1-key", LocalizedString.of(ENGLISH, "test-type-name-1"), singleton("payment"))
                .plusFieldDefinitions(FieldDefinition
                        .of(StringFieldType.of(), "string-field-1", LocalizedString.of(ENGLISH, "string-field-name-1"), false))
                .build()));
        executeBlocking(typeService.createType(TypeDraftBuilder
                .of("test-type-2-key", LocalizedString.of(GERMAN, "test-type-name-2"), singleton("payment"))
                .plusFieldDefinitions(FieldDefinition
                        .of(MoneyFieldType.of(), "money-field-2", LocalizedString.of(GERMAN, "money-field-name-2"), true))
                .build()));

        List<Type> newTypes = executeBlocking(typeService.getTypes());

        assertThat(newTypes).isNotNull();
        assertThat(newTypes).hasSize(2);

        Type type1 = newTypes.stream().filter(type -> "test-type-1-key".equals(type.getKey())).findFirst().orElseThrow(IllegalStateException::new);
        assertThat(type1.getResourceTypeIds()).containsExactly("payment");
        FieldDefinition field1 = type1.getFieldDefinitionByName("string-field-1");
        assertThat(field1).isNotNull();
        assertThat(field1.getType()).isExactlyInstanceOf(StringFieldType.class);
        assertThat(field1.isRequired()).isFalse();

        Type type2 = newTypes.stream().filter(type -> "test-type-2-key".equals(type.getKey())).findFirst().orElseThrow(IllegalStateException::new);
        assertThat(type2.getResourceTypeIds()).containsExactly("payment");
        FieldDefinition field2 = type2.getFieldDefinitionByName("money-field-2");
        assertThat(field2).isNotNull();
        assertThat(field2.getType()).isExactlyInstanceOf(MoneyFieldType.class);
        assertThat(field2.isRequired()).isTrue();
    }

    @Test
    public void updateType() throws Exception {
        Type testType = executeBlocking(typeService.createType(TypeDraftBuilder
                .of("test-type-1-key", LocalizedString.of(ENGLISH, "test-updateType-type"), singleton("payment"))
                .build()));

        executeBlocking(typeService.updateType(testType, asList(
                ChangeName.of(LocalizedString.of(new Locale("uk"), "test-update-type-uk")
                        .plus(FRENCH, "test-update-type-fr")),
                ChangeKey.of("new-test-type-key")
        )));

        List<Type> types = executeBlocking(typeService.getTypes());
        assertThat(types).hasSize(1);
        Type updatedType = types.get(0);
        assertThat(updatedType.getKey()).isEqualTo("new-test-type-key");
        assertThat(updatedType.getName().get(FRENCH)).isEqualTo("test-update-type-fr");
        assertThat(updatedType.getName().get(new Locale("uk"))).isEqualTo("test-update-type-uk");
    }

    @Test
    public void updateTypeWithEmptyList_returnsTheSameInstance() throws Exception {
        Type testType = executeBlocking(typeService.createType(TypeDraftBuilder
                .of("test-type-1-key", LocalizedString.of(ENGLISH, "test-updateType-type"), singleton("payment"))
                .build()));

        Type updatedType = executeBlocking(typeService.updateType(testType, emptyList()));
        assertThat(updatedType).isSameAs(testType);

        updatedType = executeBlocking(typeService.updateType(testType, null));
        assertThat(updatedType).isSameAs(testType);
    }

}