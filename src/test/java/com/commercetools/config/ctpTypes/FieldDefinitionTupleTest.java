package com.commercetools.config.ctpTypes;

import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.models.TextInputHint;
import io.sphere.sdk.types.DateFieldType;
import io.sphere.sdk.types.FieldDefinition;
import io.sphere.sdk.types.MoneyFieldType;
import io.sphere.sdk.types.StringFieldType;
import org.junit.Test;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;


public class FieldDefinitionTupleTest {
    @Test
    public void of() throws Exception {
        assertThat(TenantCtpTypesValidator.FieldDefinitionTuple.of(
                FieldDefinition.of(StringFieldType.of(), "blah", LocalizedString.of(), true),
                null))
                .isNull();

        assertThat(TenantCtpTypesValidator.FieldDefinitionTuple.of(
                FieldDefinition.of(StringFieldType.of(), "blah", LocalizedString.of(), true),
                FieldDefinition.of(DateFieldType.of(), "blah-2", LocalizedString.of(), false)))
                .isNotNull();
    }

    @Test
    public void whenSameOrSimilarInstances_areDifferentIsFalse() throws Exception {
        final FieldDefinition blah = FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(), true, TextInputHint.MULTI_LINE);
        final FieldDefinition blahSimilar = FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(), true, TextInputHint.MULTI_LINE);

        TenantCtpTypesValidator.FieldDefinitionTuple same = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah);
        assertFieldDefinitionTupleDifference(same, false);

        same = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blahSimilar);
        assertFieldDefinitionTupleDifference(same, false);
    }

    @Test
    public void whenSameNameTypeRequiredDistinct_areDifferentIsFalse() throws Exception {
        final FieldDefinition blah = FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(), true, TextInputHint.MULTI_LINE);
        TenantCtpTypesValidator.FieldDefinitionTuple differentLabel_InputHint = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(ENGLISH, "xxx"), true, TextInputHint.SINGLE_LINE));
        assertFieldDefinitionTupleDifference(differentLabel_InputHint, false);
    }

    @Test
    public void whenNameTypeRequiredDistinct_areDifferentIsTrue() throws Exception {
        final FieldDefinition blah = FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(), true);

        TenantCtpTypesValidator.FieldDefinitionTuple differentName = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(DateFieldType.of(), "blah-diff", LocalizedString.of(), true));
        assertFieldDefinitionTupleDifference(differentName, true);

        TenantCtpTypesValidator.FieldDefinitionTuple differentType = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(MoneyFieldType.of(), "blah", LocalizedString.of(), true));
        assertFieldDefinitionTupleDifference(differentType, true);

        TenantCtpTypesValidator.FieldDefinitionTuple differentRequired = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(MoneyFieldType.of(), "blah", LocalizedString.of(), false));
        assertFieldDefinitionTupleDifference(differentRequired, true);
    }

    private void assertFieldDefinitionTupleDifference(TenantCtpTypesValidator.FieldDefinitionTuple differentRequired, boolean areDifferent) {
        assertThat(differentRequired).isNotNull();
        assertThat(differentRequired.areDifferent()).isEqualTo(areDifferent);
    }

}