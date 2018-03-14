package com.commercetools.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static com.commercetools.util.ArgumentsUtils.requireNonBlank;
import static com.commercetools.util.ArgumentsUtils.requireNonEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


public class ArgumentsUtilsTest {
    @Test
    public void requireNonBlank_throwsException_withCustomMessage() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank(null, "null is not allowed"))
                .withMessage("null is not allowed");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank("", "empty is not allowed"))
                .withMessage("empty is not allowed");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank("    ", "spaces are not allowed"))
                .withMessage("spaces are not allowed");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank("\t\n\r", "whitespaces is not allowed"))
                .withMessage("whitespaces is not allowed");
    }

    @Test
    public void requireNonBlank_throwsException_withDefaultMessage() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank(null))
                .withMessage("String argument is expected to be non-blank");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank(""))
                .withMessage("String argument is expected to be non-blank");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank("    "))
                .withMessage("String argument is expected to be non-blank");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonBlank("\t\n\r"))
                .withMessage("String argument is expected to be non-blank");
    }

    @Test
    public void requireNonBlank_returnsValidArgument() throws Exception {
        assertThat(requireNonBlank("hello")).isEqualTo("hello");
        assertThat(requireNonBlank("   world   ")).isEqualTo("   world   ");
        assertThat(requireNonBlank("\t\r!\n\f")).isEqualTo("\t\r!\n\f");

        assertThat(requireNonBlank("hello", "message is ignored")).isEqualTo("hello");
        assertThat(requireNonBlank("   world   ", "message is ignored")).isEqualTo("   world   ");
        assertThat(requireNonBlank("\t\r!\n\f", "message is ignored")).isEqualTo("\t\r!\n\f");
    }

    @Test
    public void requireNonEmpty_throwsException_withCustomMessage() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(null, "must be not null"))
                .withMessage("must be not null");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(new ArrayList<>(), "must be not empty"))
                .withMessage("must be not empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(new HashSet<>(), "must be not empty"))
                .withMessage("must be not empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(emptyList(), "must be not empty"))
                .withMessage("must be not empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(emptySet(), "must be not empty"))
                .withMessage("must be not empty");
    }

    @Test
    public void requireNonEmpty_throwsException_withDefaultMessage() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(null))
                .withMessage("Collection argument is expected to be non-empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(new ArrayList<>()))
                .withMessage("Collection argument is expected to be non-empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(new HashSet<>()))
                .withMessage("Collection argument is expected to be non-empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(emptyList()))
                .withMessage("Collection argument is expected to be non-empty");

        assertThatIllegalArgumentException().isThrownBy(() -> requireNonEmpty(emptySet()))
                .withMessage("Collection argument is expected to be non-empty");
    }

    @Test
    public void requireNonEmpty_returnsTheCollection() throws Exception {
        HashSet<String> testSet = new HashSet<>(asList("x", "y", "z"));
        assertThat(requireNonEmpty(singletonList("hello"))).containsExactly("hello");
        assertThat(requireNonEmpty(asList(1, 2, 3))).containsExactly(1, 2, 3);
        assertThat(requireNonEmpty(testSet)).containsExactly("x", "y", "z");

        assertThat(requireNonEmpty(singletonList("world"), "ignored")).containsExactly("world");
        assertThat(requireNonEmpty(asList(1, 2, 3), "ignored")).containsExactly(1, 2, 3);
        assertThat(requireNonEmpty(testSet, "ignored")).containsExactly("x", "y", "z");
    }


}