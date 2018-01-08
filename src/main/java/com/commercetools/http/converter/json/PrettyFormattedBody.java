package com.commercetools.http.converter.json;

import javax.annotation.Nonnull;

/**
 * Class to wrap a response body if (JSON) pretty formatting could be expected in the result output.
 */
public final class PrettyFormattedBody {
    private final Object body;
    private final boolean pretty;

    private PrettyFormattedBody(@Nonnull final Object body, final boolean pretty) {
        this.body = body;
        this.pretty = pretty;
    }

    /**
     * @return real response body to output
     */
    public Object getBody() {
        return body;
    }

    /**
     * @return <b>true</b> if the {@link #body} should be pretty printed in the response.
     */
    public boolean isPretty() {
        return pretty;
    }

    public static PrettyFormattedBody of(@Nonnull final Object body, final boolean pretty) {
        return new PrettyFormattedBody(body, pretty);
    }
}