package com.commercetools.exception;

/**
 * A specific Paypal Plus exception to be used in the services. If you want to
 * throw an exception, please use this one.
 */
public class PaypalPlusPaymentException extends RuntimeException {

    public PaypalPlusPaymentException(String message) {
        super(message);
    }

}