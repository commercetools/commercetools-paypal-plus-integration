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
        assertThat(getCustomFieldStringOrDefault(null, null, "x")).contains("x");
        assertThat(getCustomFieldStringOrDefault(null, "foo", "y")).contains("y");

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "woot")).contains("woot");

        when(customFields.getFieldAsString("foo")).thenReturn(null);
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "hack")).contains("hack");

        when(customFields.getFieldAsString("foo")).thenReturn("");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "ha-ha")).contains("");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, null, "ha-ha")).contains("ha-ha");
    }

    @Test
    public void getCustomFieldStringOrDefault_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsString("foo")).thenReturn("bar");
        when(customFields.getFieldAsString("bar")).thenReturn("woot");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "foo", "no no David Blaine")).contains("bar");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "bar", "no no David Blaine")).contains("woot");
        assertThat(getCustomFieldStringOrDefault(customFieldsHolder, "batman", "YES David Blaine")).contains("YES David Blaine");
    }

    @Test
    public void getCustomFieldStringOrEmpty_defaultCases() throws Exception {
        assertThat(getCustomFieldStringOrEmpty(null, null)).contains("");
        assertThat(getCustomFieldStringOrEmpty(null, "foo")).contains("");

        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).contains("");

        when(customFields.getFieldAsString("foo")).thenReturn(null);
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).contains("");

        when(customFields.getFieldAsString("foo")).thenReturn("");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).contains("");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, null)).contains("");
    }

    @Test
    public void getCustomFieldStringOrEmpty_normalCases() throws Exception {
        when(customFieldsHolder.getCustom()).thenReturn(customFields);
        when(customFields.getFieldAsString("foo")).thenReturn("bar");
        when(customFields.getFieldAsString("bar")).thenReturn("woot");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "foo")).contains("bar");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "bar")).contains("woot");
        assertThat(getCustomFieldStringOrEmpty(customFieldsHolder, "spider-man")).contains("");
    }

}