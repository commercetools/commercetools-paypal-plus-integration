package com.commercetools.exception;

/**
 * A general Paypal Plus exception to be used
 * with any exceptions connected with Paypal Plus
 */
public class PaypalPlusException extends RuntimeException {

    public PaypalPlusException(String message) {
        super(message);
    }

}