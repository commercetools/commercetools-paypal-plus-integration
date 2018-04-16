package com.commercetools.controller;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.io.IOException;

import static com.commercetools.testUtil.JsonAssertUtil.assertJsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static testUtil.HttpTestUtil.*;

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
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(response.getEntity().getContentType().getValue())
                .startsWith(ContentType.APPLICATION_JSON.getMimeType());

        String content = getContent(response);

        assertJsonPath(content, "$.tenants", hasSize(greaterThanOrEqualTo(1)));
        assertJsonPath(content, "$.applicationInfo.version", not(isEmptyOrNullString()));
        assertJsonPath(content, "$.applicationInfo.title", is(equalTo("commercetools-paypalplus-integration")));
    }
}