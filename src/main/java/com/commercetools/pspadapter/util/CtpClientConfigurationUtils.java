package com.commercetools.pspadapter.util;

import io.sphere.sdk.client.SphereAccessTokenSupplier;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereClientConfig;
import io.sphere.sdk.http.AsyncHttpClientAdapter;
import io.sphere.sdk.http.HttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class CtpClientConfigurationUtils {
    private static final long DEFAULT_TIMEOUT = 30;
    private static final TimeUnit DEFAULT_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * Creates a {@link SphereClient} with a default {@code timeout} value of 30 seconds for handshake.
     *
     * @return the instantiated {@link SphereClient}.
     */
    @Nonnull
    public static SphereClient createSphereClient(@Nonnull final SphereClientConfig clientConfig) {
        final HttpClient httpClient = createHttpClient();
        final SphereAccessTokenSupplier tokenSupplier =
                SphereAccessTokenSupplier.ofAutoRefresh(clientConfig, httpClient, false);
        return SphereClient.of(clientConfig, httpClient, tokenSupplier);
    }

    /**
     * Gets an asynchronous {@link HttpClient} to be used by the {@link SphereClient}.
     *
     * @return {@link HttpClient}
     */
    @Nonnull
    private static HttpClient createHttpClient() {
        return AsyncHttpClientAdapter.of(new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder()
                        .setHandshakeTimeout((int) DEFAULT_TIMEOUT_TIME_UNIT.toMillis(DEFAULT_TIMEOUT)).build()));
    }
}
