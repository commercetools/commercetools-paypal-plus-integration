package com.commercetools.exception;

/**
 * A general Paypal Plus exception to be used application-wide. If you want to
 * throw an exception, please use this one or extend it.
 */
public class PaypalPlusException extends RuntimeException {

    public PaypalPlusException(String message) {
        super(message);
    }

}