package com.commercetools.pspadapter.paymentHandler.impl;

import org.springframework.http.HttpStatus;

public class PaymentHandleResult {

    private final HttpStatus statusCode;

    private final String body;

    public PaymentHandleResult(final HttpStatus statusCode, final String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public PaymentHandleResult(final HttpStatus statusCode) {
        this(statusCode, "");
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
