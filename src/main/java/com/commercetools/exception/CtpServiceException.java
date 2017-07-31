package com.commercetools.exception;

public class CtpServiceException extends RuntimeException {
    public CtpServiceException(String message) {
        super(message);
    }

    public CtpServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CtpServiceException(Throwable cause) {
        super(cause);
    }
}
