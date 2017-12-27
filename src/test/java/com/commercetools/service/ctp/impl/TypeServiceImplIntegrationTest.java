package com.commercetools.service.ctp.impl;

import com.commercetools.Application;
import com.commercetools.service.ctp.TypeService;
import com.commercetools.testUtil.customTestConfigs.TypesCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.types.*;
import io.sphere.sdk.types.commands.updateactions.ChangeKey;
import io.sphere.sdk.types.commands.updateactions.ChangeName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Locale;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupTypes;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Locale.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Import(value = {TypesCleanupConfiguration.class})
public class TypeServiceImplIntegrationTest {

    @Autowired
    private SphereClient sphereClient;

    @Autowired
    private TypeService typeService;

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
    public void addFieldDefinitions() throws Exception {
        Type testType = executeBlocking(typeService.createType(TypeDraftBuilder
                .of("test-type-1-key", LocalizedString.of(ENGLISH, "test-addFieldDefinitions-type"), singleton("payment"))
                .build()));

        executeBlocking(typeService.addFieldDefinitions(testType, asList(
                FieldDefinition.of(StringFieldType.of(), "string-field-name",
                        LocalizedString.of(ENGLISH, "string-field-en").plus(GERMAN, "string-field-de"),
                        false),
                FieldDefinition.of(DateFieldType.of(), "date-field-name",
                        LocalizedString.of(CHINESE, "date-field-zh"),
                        true)
        )));

        Type updatedType = executeBlocking(typeService.getTypes()).stream()
                .filter(t -> "test-type-1-key".equals(t.getKey())).findFirst().orElseThrow(IllegalStateException::new);

        assertThat(updatedType).isNotNull();
        assertThat(updatedType.getFieldDefinitions()).hasSize(2);

        FieldDefinition stringField = updatedType.getFieldDefinitionByName("string-field-name");
        assertThat(stringField).isNotNull();
        assertThat(stringField.getType()).isInstanceOf(StringFieldType.class);
        assertThat(stringField.getLabel())
                .isEqualTo(LocalizedString.of(ENGLISH, "string-field-en").plus(GERMAN, "string-field-de"));
        assertThat(stringField.isRequired())
                .isEqualTo(false);

        FieldDefinition dateField = updatedType.getFieldDefinitionByName("date-field-name");
        assertThat(dateField).isNotNull();
        assertThat(dateField.getType()).isInstanceOf(DateFieldType.class);
        assertThat(dateField.getLabel())
                .isEqualTo(LocalizedString.of(CHINESE, "date-field-zh"));
        assertThat(dateField.isRequired())
                .isEqualTo(true);
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

}