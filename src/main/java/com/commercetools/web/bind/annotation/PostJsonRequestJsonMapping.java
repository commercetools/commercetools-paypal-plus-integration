package com.commercetools.web.bind.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Custom annotation for {@code POST} endpoints which accept (consume) and response (produce)
 * <i>application/json</i> mime-type.
 *
 * @see PostJsonRequestJsonMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PostRequestJsonMapping(consumes = APPLICATION_JSON_VALUE)
public @interface PostJsonRequestJsonMapping {
    @AliasFor(annotation = RequestMapping.class)
    String name() default "";

    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] path() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] params() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};
}
