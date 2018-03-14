package com.commercetools.testUtil;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matcher;
import org.springframework.test.util.JsonPathExpectationsHelper;

public final class JsonAssertUtil {

    /**
     * Assert {@code content} string is JSON-formatted and contains {@code jsonPath} value matching {@code matcher}.
     * <p>
     * Example:<pre>
     * assertJsonPath("{\"key\": \"value\"}", "$.key", is("value"));
     * assertJsonPath("{\"arrays\":{\"arrayKey\": [1, 2, 3]}}", "$.arrays.arrayKey", contains(1, 2, 3));
     * </pre>
     *
     * @param content  JSON string content
     * @param jsonPath json path which value to assert.
     *                 See more at {@link JsonPath#compile(java.lang.String, com.jayway.jsonpath.Predicate...)}
     * @param matcher  matcher to apply to the value fetched from {@code jsonPath}
     */
    public static void assertJsonPath(String content, String jsonPath, Matcher<?> matcher) {
        (new JsonPathExpectationsHelper(jsonPath)).assertValue(content, matcher);
    }

    private JsonAssertUtil() {
    }
}
