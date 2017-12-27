package com.commercetools.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Arguments verification utils: verify the arguments are valid (for certain purpose),
 * i.e. non-empty, non-blank and so on.
 */
public final class ArgumentsUtils {

    /**
     * Return {@code argument} if is a non-null non-blank string, otherwise throw {@link IllegalArgumentException}
     * with {@code message}
     *
     * @param argument value to verify
     * @param message  message to set into thrown {@link IllegalArgumentException} if the {@code argument} is null or
     *                 blank
     * @return {@code argument}
     * @throws IllegalArgumentException if {@code argument} is null or a blank string.
     */
    @Nonnull
    public static String requireNonBlank(@Nullable String argument, @Nonnull String message) {
        if (isBlank(argument)) {
            throw new IllegalArgumentException(message);
        } else {
            return argument;
        }
    }

    /**
     * Return {@code argument} if is a non-null non-blank string, otherwise throw {@link IllegalArgumentException}
     * with default message.
     *
     * @param argument value to verify
     * @return {@code argument}
     * @throws IllegalArgumentException if {@code argument} is null or a blank string.
     */
    @Nonnull
    public static String requireNonBlank(@Nullable String argument) {
        return requireNonBlank(argument, "String argument is expected to be non-blank");
    }

    /**
     * Return {@code collection} argument if is a non-null non-empty collection,
     * otherwise throw {@link IllegalArgumentException} with {@code message}
     *
     * @param collection value to verify
     * @param message    message to set into thrown {@link IllegalArgumentException} if the {@code collection}
     *                   is null or empty
     * @return {@code collection}
     * @throws IllegalArgumentException if the {@code collection} is null or a empty collection.
     */
    @Nonnull
    public static <T, C extends Collection<T>> C requireNonEmpty(@Nullable C collection, @Nonnull String message) {
        if (isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        } else {
            return collection;
        }
    }

    /**
     * Return {@code collection} argument if is a non-null non-empty collection,
     * otherwise throw {@link IllegalArgumentException} with {@code message}
     *
     * @param collection value to verify
     * @return {@code collection}
     * @throws IllegalArgumentException if the {@code collection} is null or a empty collection.
     */
    @Nonnull
    public static <T, C extends Collection<T>> C requireNonEmpty(@Nullable C collection) {
        return requireNonEmpty(collection, "Collection argument is expected to be non-empty");
    }

    private ArgumentsUtils() {
    }
}
