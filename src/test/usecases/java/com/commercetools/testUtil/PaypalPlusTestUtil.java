package com.commercetools.testUtil;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public final class PaypalPlusTestUtil {

    public static final String SANDBOX_URL_PREFIX = "https://www.sandbox.paypal.com/";
    public static final Pattern TOKEN_URL_PATTERN = Pattern.compile("token=[\\w\\-]");


    /**
     * Assert the {@code approvalUrl} has expected values.
     * <p>
     * <b>Of course, it is better to implement Selenium tests here with real URL navigation,
     * but when we have more time. For now just validate returned URL.</b>
     *
     * @param approvalUrl value to validate
     */
    public static void assertApprovalUrl(String approvalUrl) {
        assertThat(approvalUrl).startsWith(SANDBOX_URL_PREFIX);
        assertThat(approvalUrl).containsPattern(TOKEN_URL_PATTERN);
    }

    private PaypalPlusTestUtil() {
    }
}
