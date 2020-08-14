package com.commercetools.exception;

import com.paypal.base.rest.PayPalRESTException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Exception wrapper for checked {@link PayPalRESTException}. It has a sense to use it in completion stages chains
 * where exception is handled for the whole change by special catcher.
 */
public class PaypalPlusServiceException extends RuntimeException {

    private final PayPalRESTException cause;

    public PaypalPlusServiceException(@Nonnull String message) {
        super(message);
        this.cause = null;
    }

    public PaypalPlusServiceException(@Nonnull String message, @Nullable PayPalRESTException cause) {
        super(message, cause);
        this.cause = cause;
    }

    public PaypalPlusServiceException(@Nullable PayPalRESTException cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    @Nullable
    public synchronized PayPalRESTException getCause() {
        return cause;
    }
}
