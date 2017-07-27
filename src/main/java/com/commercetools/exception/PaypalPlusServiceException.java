package com.commercetools.exception;

import com.paypal.base.rest.PayPalRESTException;

import javax.annotation.Nullable;

/**
 * Exception wrapper for checked {@link PayPalRESTException}. It has a sense to use it in completion stages chains
 * where exception is handled for the whole change by special catcher.
 */
public class PaypalPlusServiceException extends RuntimeException {

    private final PayPalRESTException cause;

    public PaypalPlusServiceException(String message) {
        super(message);
        this.cause = null;
    }

    public PaypalPlusServiceException(String message, PayPalRESTException cause) {
        super(message, cause);
        this.cause = cause;
    }

    public PaypalPlusServiceException(PayPalRESTException cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    @Nullable
    public PayPalRESTException getCause() {
        return cause;
    }
}