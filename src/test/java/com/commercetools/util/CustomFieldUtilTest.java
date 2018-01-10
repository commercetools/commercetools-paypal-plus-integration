package com.commercetools.util;

import io.sphere.sdk.types.Custom;
import io.sphere.sdk.types.CustomFields;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.commercetools.util.CustomFieldUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomFieldUtilTest {

    @Mock
    private Custom customFieldsHolder;

    @Mock
    private CustomFields customFields;

    @Test
    public void getCustomFieldString_emptyCases() throws Exception {
        assertThat(getCustomFieldString(null, null)).isEmpty();
        assertThat(getCustomFieldString(null, "foo")).isEmpty();

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldString(customFieldsHolder, "foo")).isEmpty();

        when(customFields.getFieldAsString("foo")).thenReturn(null);
        assertThat(getCustomFieldString(customFieldsHolder, "foo")).isEmpty();

        when(customFields.getFieldAsString("foo")).thenReturn("");
        assertThat(getCustomFieldString(customFieldsHolder, "foo")).contains("");
        assertThat(getCustomFieldString(customFieldsHolder, null)).isEmpty();
    }

    @Test
    public void getCustomFieldString_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsString("foo")).thenReturn("bar");
        when(customFields.getFieldAsString("bar")).thenReturn("woot");
        assertThat(getCustomFieldString(customFieldsHolder, "foo")).contains("bar");
        assertThat(getCustomFieldString(customFieldsHolder, "bar")).contains("woot");
    }

    @Test
    public void getCustomFieldStringOrDefault_defaultCases() throws Exception {
        assertThat(getCustomFieldStringOrDefault(null, null, "x")).isEqualTo("x");
        assertThat(getCustomFieldStringOrDefault(null, "foo", "y")).isEqualTo("y");

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "woot")).isEqualTo("woot");

        when(customFields.getFieldAsString("foo")).thenReturn(null);
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "hack")).isEqualTo("hack");

        when(customFields.getFieldAsString("foo")).thenReturn("");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "ha-ha")).isEqualTo("");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, null, "ha-ha")).isEqualTo("ha-ha");
    }

    @Test
    public void getCustomFieldStringOrDefault_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsString("foo")).thenReturn("bar");
        when(customFields.getFieldAsString("bar")).thenReturn("woot");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "no no David Blaine")).isEqualTo("bar");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "bar", "no no David Blaine")).isEqualTo("woot");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "batman", "YES David Blaine")).isEqualTo("YES David Blaine");
    }

    @Test
    public void getCustomFieldStringOrEmpty_defaultCases() throws Exception {
        assertThat(getCustomFieldStringOrEmpty(null, null)).isEqualTo("");
        assertThat(getCustomFieldStringOrEmpty(null, "foo")).isEqualTo("");

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).isEqualTo("");

        when(customFields.getFieldAsString("foo")).thenReturn(null);
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).isEqualTo("");

        when(customFields.getFieldAsString("foo")).thenReturn("");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).isEqualTo("");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, null)).isEqualTo("");
    }

    @Test
    public void getCustomFieldStringOrEmpty_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsString("foo")).thenReturn("bar");
        when(customFields.getFieldAsString("bar")).thenReturn("woot");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).isEqualTo("bar");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "bar")).isEqualTo("woot");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "spider-man")).isEqualTo("");
    }

    @Test
    public void getCustomFieldStringOrNull_defaultCases() throws Exception {
        assertThat(getCustomFieldStringOrNull(null, null)).isNull();
        assertThat(getCustomFieldStringOrNull(null, "foo")).isNull();

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, "foo")).isNull();

        when(customFields.getFieldAsString("foo")).thenReturn(null);
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, "foo")).isNull();

        when(customFields.getFieldAsString("foo")).thenReturn("");
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, "foo")).isEqualTo("");
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, null)).isNull();
    }

    @Test
    public void getCustomFieldStringOrNull_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsString("foo")).thenReturn("bar");
        when(customFields.getFieldAsString("bar")).thenReturn("woot");
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, "foo")).isEqualTo("bar");
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, "bar")).isEqualTo("woot");
        assertThat(getCustomFieldStringOrNull(customFieldsHolder, "spider-man")).isNull();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Enum
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void getCustomFieldEnumKey_emptyCases() throws Exception {
        assertThat(getCustomFieldEnumKey(null, null)).isEmpty();
        assertThat(getCustomFieldEnumKey(null, "foo")).isEmpty();

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldEnumKey(customFieldsHolder, "foo")).isEmpty();

        when(customFields.getFieldAsEnumKey("foo")).thenReturn(null);
        assertThat(getCustomFieldEnumKey(customFieldsHolder, "foo")).isEmpty();

        when(customFields.getFieldAsEnumKey("foo")).thenReturn("");
        assertThat(getCustomFieldEnumKey(customFieldsHolder, "foo")).contains("");
        assertThat(getCustomFieldEnumKey(customFieldsHolder, null)).isEmpty();
    }

    @Test
    public void getCustomFieldEnumKey_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsEnumKey("foo")).thenReturn("bar");
        when(customFields.getFieldAsEnumKey("bar")).thenReturn("woot");
        assertThat(getCustomFieldEnumKey(customFieldsHolder, "foo")).contains("bar");
        assertThat(getCustomFieldEnumKey(customFieldsHolder, "bar")).contains("woot");
    }

    @Test
    public void getCustomFieldEnumKeyOrNull_defaultCases() throws Exception {
        assertThat(getCustomFieldEnumKeyOrNull(null, null)).isNull();
        assertThat(getCustomFieldEnumKeyOrNull(null, "foo")).isNull();

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, "foo")).isNull();

        when(customFields.getFieldAsEnumKey("foo")).thenReturn(null);
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, "foo")).isNull();

        when(customFields.getFieldAsEnumKey("foo")).thenReturn("");
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, "foo")).isEqualTo("");
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, null)).isNull();
    }

    @Test
    public void getCustomFieldEnumKeyOrNull_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsEnumKey("foo")).thenReturn("bar");
        when(customFields.getFieldAsEnumKey("bar")).thenReturn("woot");
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, "foo")).isEqualTo("bar");
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, "bar")).isEqualTo("woot");
        assertThat(getCustomFieldEnumKeyOrNull(customFieldsHolder, "spider-man")).isNull();
    }
}