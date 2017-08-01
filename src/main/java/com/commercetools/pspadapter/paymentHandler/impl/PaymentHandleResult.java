package com.commercetools.pspadapter.paymentHandler.impl;

import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;

/**
 * @deprecated use {@link PaymentHandleResponse}
 */
@Deprecated
public class PaymentHandleResult {

    private final HttpStatus statusCode;

    private final String body;

    public PaymentHandleResult(@Nonnull HttpStatus statusCode, @Nonnull String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public PaymentHandleResult(@Nonnull HttpStatus statusCode) {
        this(statusCode, "");
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
