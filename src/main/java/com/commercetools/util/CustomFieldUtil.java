package com.commercetools.util;

import io.sphere.sdk.types.Custom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public final class CustomFieldUtil {

    /**
     * Get string custom field value from {@code customFieldHolder} if exists.
     *
     * @param customFieldHolder instance which has custom fields
     * @param fieldName         name of the custom field to fetch
     * @return string value of the custom field, or {@link Optional#empty()} if not found.
     */
    public static Optional<String> getCustomFieldString(@Nullable Custom customFieldHolder,
                                                        @Nullable String fieldName) {
        return ofNullable(customFieldHolder)
                .map(Custom::getCustom)
                .map(customFields -> customFields.getFieldAsString(fieldName));
    }

    /**
     * Get string custom field value from {@code customFieldHolder} if exists, or {@code defaultValue} otherwise.
     * <p>
     * <b>Note:</b> if the custom field exists, but the value is <b>null</b> - {@code defaultValue} is returned.
     *
     * @param customFieldHolder instance which has custom fields
     * @param fieldName         name of the custom field to fetch
     * @param defaultValue      value to return if custom field can't be found or is <b>null</b>
     * @return string value of the custom field if exists and is non-null, otherwise {@code defaultValue}.
     */
    @Nonnull
    public static String getCustomFieldStringOrDefault(@Nullable Custom customFieldHolder,
                                                       @Nullable String fieldName,
                                                       @Nonnull String defaultValue) {
        return getCustomFieldString(customFieldHolder, fieldName).orElse(defaultValue);
    }

    /**
     * Get string custom field value from {@code customFieldHolder} if exists and non-null,
     * otherwise return empty string.
     *
     * @param customFieldHolder instance which has custom fields
     * @param fieldName         name of the custom field to fetch
     * @return string value of the custom field if exists and non-null, otherwise empty string.
     */
    @Nonnull
    public static String getCustomFieldStringOrEmpty(@Nullable Custom customFieldHolder,
                                                     @Nullable String fieldName) {
        return getCustomFieldStringOrDefault(customFieldHolder, fieldName, "");
    }

    private CustomFieldUtil() {
    }
}
