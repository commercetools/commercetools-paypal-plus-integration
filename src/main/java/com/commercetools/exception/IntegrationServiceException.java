package com.commercetools.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Custom exception to handle unexpected errors that might occur while communicating with outside service endpoints
 */
public class IntegrationServiceException extends RuntimeException {

    private final Throwable cause;

    public IntegrationServiceException(@Nonnull String message) {
        super(message);
        this.cause = null;
    }

    public IntegrationServiceException(@Nonnull String message, @Nullable Throwable cause) {
        super(message, cause);
        this.cause = cause;
    }

    public IntegrationServiceException(@Nullable Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    @Nullable
    public Throwable getCause() {
        return cause;
    }
}
