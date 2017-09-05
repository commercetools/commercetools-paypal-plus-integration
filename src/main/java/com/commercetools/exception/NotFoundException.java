package com.commercetools.exception;

/**
 * A resource does not exist anymore
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}