package com.commercetools.config.ctpTypes;

import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.models.LocalizedEnumValue;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.models.TextInputHint;
import io.sphere.sdk.types.*;
import org.junit.Test;

import static java.util.Arrays.asList;
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
        assertFieldDefinitionTupleHasEqual(same);

        same = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blahSimilar);
        assertFieldDefinitionTupleHasEqual(same);
    }

    @Test
    public void whenSameNameTypeRequiredDistinct_areDifferentIsFalse() throws Exception {
        final FieldDefinition blah = FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(), true, TextInputHint.MULTI_LINE);
        TenantCtpTypesValidator.FieldDefinitionTuple differentLabel_InputHint = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(ENGLISH, "xxx"), true, TextInputHint.SINGLE_LINE));
        assertFieldDefinitionTupleHasEqual(differentLabel_InputHint);
    }

    @Test
    public void whenNameTypeRequiredDistinct_areDifferentIsTrue() throws Exception {
        final FieldDefinition blah = FieldDefinition.of(DateFieldType.of(), "blah", LocalizedString.of(), true);

        TenantCtpTypesValidator.FieldDefinitionTuple differentName = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(DateFieldType.of(), "blah-diff", LocalizedString.of(), true));
        assertFieldDefinitionTupleHasDifferent(differentName);

        TenantCtpTypesValidator.FieldDefinitionTuple differentType = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(MoneyFieldType.of(), "blah", LocalizedString.of(), true));
        assertFieldDefinitionTupleHasDifferent(differentType);

        TenantCtpTypesValidator.FieldDefinitionTuple differentRequired = TenantCtpTypesValidator.FieldDefinitionTuple.of(blah,
                FieldDefinition.of(MoneyFieldType.of(), "blah", LocalizedString.of(), false));
        assertFieldDefinitionTupleHasDifferent(differentRequired);
    }

    @Test
    public void areDifferent_ForReferenceType() throws Exception {
        // different
        FieldDefinition blah = FieldDefinition.of(ReferenceFieldType.of("product"), "blah", LocalizedString.of(), true);
        FieldDefinition blah2 = FieldDefinition.of(ReferenceFieldType.of("product-type"), "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasDifferent(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));

        // same
        blah = FieldDefinition.of(ReferenceFieldType.of("product"), "blah", LocalizedString.of(), true);
        blah2 = FieldDefinition.of(ReferenceFieldType.of("product"), "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));
    }

    @Test
    public void areDifferent_ForSetType() throws Exception {
        // different
        FieldDefinition blah = FieldDefinition.of(SetFieldType.of(DateFieldType.of()), "blah", LocalizedString.of(), true);
        FieldDefinition blah2 = FieldDefinition.of(SetFieldType.of(StringFieldType.of()), "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasDifferent(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));

        // same
        blah = FieldDefinition.of(SetFieldType.of(MoneyFieldType.of()), "blah", LocalizedString.of(), true);
        blah2 = FieldDefinition.of(SetFieldType.of(MoneyFieldType.of()), "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));
    }

    @Test
    public void areDifferent_ForEnumType_isAlwaysFalse() throws Exception {
        // equal fields
        FieldDefinition blah = FieldDefinition.of(EnumFieldType.of(asList(
                EnumValue.of("AAA", "ignore1"),
                EnumValue.of("BBB", "ignore2"))),
                "blah", LocalizedString.of(), true);
        FieldDefinition blah2 = FieldDefinition.of(EnumFieldType.of(asList(
                EnumValue.of("AAA", "ignore3"),
                EnumValue.of("BBB", "ignore4"))),
                "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));

        // enum type is recoverable even with different values set - they just will be added
        blah = FieldDefinition.of(EnumFieldType.of(asList(
                EnumValue.of("AAA", "ignore1"),
                EnumValue.of("BBB", "ignore2"))), "blah", LocalizedString.of(), true);
        blah2 = FieldDefinition.of(EnumFieldType.of(asList(
                EnumValue.of("BBB", "ignore3"),
                EnumValue.of("CCC", "ignore4"))), "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));
    }

    @Test
    public void areDifferent_ForLocalizedEnumType_isAlwaysFalse() throws Exception {
        // equal fields
        FieldDefinition blah = FieldDefinition.of(LocalizedEnumFieldType.of(asList(
                LocalizedEnumValue.of("AAA", LocalizedString.of(ENGLISH, "ignore1")),
                LocalizedEnumValue.of("BBB", LocalizedString.of(ENGLISH, "ignore2")))),
                "blah", LocalizedString.of(), true);
        FieldDefinition blah2 = FieldDefinition.of(LocalizedEnumFieldType.of(asList(
                LocalizedEnumValue.of("AAA", LocalizedString.of(ENGLISH, "ignore3")),
                LocalizedEnumValue.of("BBB", LocalizedString.of(ENGLISH, "ignore4")))),
                "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));

        // lenum type is recoverable even with different values set - they just will be added
        blah = FieldDefinition.of(LocalizedEnumFieldType.of(asList(
                LocalizedEnumValue.of("AAA", LocalizedString.of(ENGLISH, "ignore1")),
                LocalizedEnumValue.of("BBB", LocalizedString.of(ENGLISH, "ignore2")))),
                "blah", LocalizedString.of(), true);
        blah2 = FieldDefinition.of(LocalizedEnumFieldType.of(asList(
                LocalizedEnumValue.of("DDD", LocalizedString.of(ENGLISH, "ignore3")),
                LocalizedEnumValue.of("CCC", LocalizedString.of(ENGLISH, "ignore4")))),
                "blah", LocalizedString.of(), true);
        assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple.of(blah, blah2));
    }

    private void assertFieldDefinitionTupleHasDifferent(TenantCtpTypesValidator.FieldDefinitionTuple differentFields) {
        assertThat(differentFields).isNotNull();
        assertThat(differentFields.areDifferent()).isTrue();
    }

    private void assertFieldDefinitionTupleHasEqual(TenantCtpTypesValidator.FieldDefinitionTuple equalFields) {
        assertThat(equalFields).isNotNull();
        assertThat(equalFields.areDifferent()).isFalse();
    }

}