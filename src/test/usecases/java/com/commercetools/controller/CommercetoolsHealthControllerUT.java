package com.commercetools.controller;

import org.apache.http.HttpResponse;
import org.junit.Test;

import java.io.IOException;

import static com.commercetools.testUtil.JsonAssertUtil.assertJsonPath;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static com.commercetools.testUtil.HttpTestUtil.*;

/**
 * Similar to {@code CommercetoolsHealthControllerIT}, but simplified and runs over running service making real URL
 * GET/POST requests.
 */
public class CommercetoolsHealthControllerUT {

    @Test
    public void checkHealth() throws IOException {
        // get
        assertResponse(executeGetRequest(""));
        assertResponse(executeGetRequest("/"));
        assertResponse(executeGetRequest("/health"));
        assertResponse(executeGetRequest("/health/"));

        // post
        assertResponse(executePostRequest(""));
        assertResponse(executePostRequest("/"));
        assertResponse(executePostRequest("/health"));
        assertResponse(executePostRequest("/health/"));
    }

    private void assertResponse(HttpResponse response) throws IOException {
        assertResponseJson(response);
    }

    private void assertResponseJson(HttpResponse response) throws IOException {
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(SC_OK);
        assertThat(response.getEntity().getContentType().getValue())
                .startsWith(APPLICATION_JSON);

        String content = getContent(response);

        assertJsonPath(content, "$.tenants", hasSize(greaterThanOrEqualTo(1)));
        assertJsonPath(content, "$.applicationInfo.version", not(isEmptyOrNullString()));
        assertJsonPath(content, "$.applicationInfo.title", is(equalTo("commercetools-paypalplus-integration")));
    }
}