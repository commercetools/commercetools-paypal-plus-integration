package com.commercetools.testUtil;

import io.sphere.sdk.commands.UpdateAction;
import org.assertj.core.api.ListAssert;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class AssertUtil {

    /**
     * Explicitly cast {@code ListAssert<? extends UpdateAction<T>>} to {@code ListAssert<UpdateAction<T>>} to avoid
     * compilation fail in the asserts with varargs.
     * <p>
     * Example:
     * <pre>
     * assertThat(commandCaptor.getValue().getUpdateActions())
     *     .containsExactly(SetInterfaceId.of("42"));
     * </pre>
     * <p>
     * causes compilation issue (after applying <a href="https://github.com/tbroyer/gradle-errorprone-plugin">error prone plugin</a>)
     * because {@link io.sphere.sdk.payments.commands.updateactions.SetInterfaceId SetInterfaceId} can't be captured
     * by {@code ? extends UpdateAction<Payment>}
     *
     * @param actual list of update actions to cast to ListAssert
     * @param <T> type of update actions
     * @return {@code ListAssert<UpdateAction<T>>)
     */
    public static <T> ListAssert<UpdateAction<T>> assertThatUpdateActionList(List<? extends UpdateAction<T>> actual) {
        return assertThat(actual);
    }
}
