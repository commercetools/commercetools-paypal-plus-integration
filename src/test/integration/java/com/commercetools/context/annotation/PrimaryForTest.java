package com.commercetools.context.annotation;

import org.springframework.context.annotation.Primary;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a primary bean for testing (usually integration) purpose.
 * <p>
 * The annotation behaves same as {@link Primary} and just a convenient alias name to specify it is a substitute for
 * the tests. Usually these beans contain some empty or simplified implementation, or some extended counters, captures,
 * mocks to be asserted and verified during the tests.
 *
 * @see Primary
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Primary
public @interface PrimaryForTest {
}
