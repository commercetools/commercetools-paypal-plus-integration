package com.commercetools.testUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.IOException;

import static com.commercetools.util.IOUtil.DEFAULT_CHARSET;
import static java.lang.String.format;

/**
 * Utils around <a href="https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html">Apache Fluent API</a>
 * to perform HTTP GET/POST requests in the usecase tests.
 * <p>
 * One of the features of the util - re-wrap checked exceptions to simplify usages in the test.
 * <p>
 * Fot now target host:port are hardcoded in the implementation to <code><b>http://localhost:8080/</b></code>,
 * but might be extended in the future to be more flexible if run from gradle build script.
 *
 * @see <a href="https://hc.apache.org/httpcomponents-client-ga/tutorial/html/fluent.html">Apache Fluent API</a>
 */
public final class HttpTestUtil {

    private static final String HOST = "http://localhost";
    private static final int PORT = 8080;
    public static final String BASE_URI = format("%s:%d/", HOST, PORT);

    /**
     * Execute request to {@link #BASE_URI} and return {@link HttpResponse}
     *
     * @param request {@link Request} to execute.
     * @return {@link org.apache.http.client.fluent.Response Response}
     */
    @Nonnull
    public static HttpResponse executeRequest(@Nonnull Request request) {
        try {
            return request.execute().returnResponse();
        } catch (IOException e) {
            throw new RuntimeException("Exception executing HTTP request:\n", e);
        }
    }

    @Nonnull
    public static HttpResponse executeGetRequest(@Nonnull String relativePath) {
        return executeRequest(Request.Get(BASE_URI + relativePath));
    }

    @Nonnull
    public static HttpResponse executePostRequest(@Nonnull String relativePath) {
        return executeRequest(Request.Post(BASE_URI + relativePath));
    }

    @Nonnull
    public static HttpResponse executePostRequest(@Nonnull String relativePathFormat, @Nonnull Object ...args) {
        return executePostRequest(format(relativePathFormat, args));
    }

    @Nonnull
    public static String getContent(@Nonnull HttpResponse response)  {
        try {
            return new String(EntityUtils.toByteArray(response.getEntity()), DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new RuntimeException("Exception parsing HTTP response:\n", e);
        }
    }

    @Nonnull
    public static JSONObject getContentAsJsonObject(@Nonnull HttpResponse response) {
        String content = getContent(response);
        try {
            return new JSONObject(content);
        } catch (JSONException e) {
            throw new RuntimeException(format("Exception parsing JSON object:\n%s\nException:\n", content), e);
        }
    }

    private HttpTestUtil() {
    }
}
