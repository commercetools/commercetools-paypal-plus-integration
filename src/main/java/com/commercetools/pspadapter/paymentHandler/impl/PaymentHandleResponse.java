package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.payment.handler.CommercetoolsCreatePaymentsController;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.springframework.http.HttpStatus.*;

/**
 * Response body of the service, which will be converted to JSON. It contains:<ul>
 * <li>{@code statusCode} - same as HTTP response status</li>
 * <li>{@code errorMessage} (optional) - in case of 4xx and 5xx errors</li>
 * <li>{@code approvalUrl} (optional) - if response of
 * {@link CommercetoolsCreatePaymentsController#createPayment(java.lang.String, java.lang.String)} is successful (201)
 * this field contains Paypal Plus approve URL
 * </li>
 * </ul>
 * <p>
 * Use {@link #toResponseEntity()} to convert this response to standard Spring REST response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentHandleResponse {

    private final HttpStatus httpStatus;

    private final int statusCode;

    private final String errorCode;

    private final String errorMessage;

    private final String approvalUrl;

    private PaymentHandleResponse(@Nonnull HttpStatus httpStatus, @Nullable String errorCode,
                                  @Nullable String errorMessage, @Nullable String approvalUrl) {
        this.httpStatus = httpStatus;
        this.statusCode = httpStatus.value();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.approvalUrl = approvalUrl;
    }

    public static PaymentHandleResponse ofHttpStatus(HttpStatus httpStatus) {
        return new PaymentHandleResponse(httpStatus, null,null, null);
    }

    public static PaymentHandleResponse ofHttpStatusAndErrorMessage(HttpStatus httpStatus, String errorMessage) {
        return new PaymentHandleResponse(httpStatus, null, errorMessage, null);
    }

    public static PaymentHandleResponse of201CreatedApprovalUrl(@Nonnull String approvalUrl) {
        return new PaymentHandleResponse(CREATED,  null,null, approvalUrl);
    }

    public static PaymentHandleResponse of400BadRequest(@Nonnull String errorMessage) {
        return new PaymentHandleResponse(BAD_REQUEST, null, errorMessage, null);
    }

    public static PaymentHandleResponse of400BadRequest(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new PaymentHandleResponse(BAD_REQUEST, errorCode, errorMessage, null);
    }

    public static PaymentHandleResponse of404NotFound(@Nonnull String errorMessage) {
        return new PaymentHandleResponse(NOT_FOUND, null, errorMessage, null);
    }

    public static PaymentHandleResponse of404NotFound(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new PaymentHandleResponse(NOT_FOUND, errorCode, errorMessage, null);
    }

    public static PaymentHandleResponse of500InternalServerError(@Nonnull String errorMessage) {
        return new PaymentHandleResponse(INTERNAL_SERVER_ERROR, null, errorMessage, null);
    }

    public static PaymentHandleResponse of500InternalServerError(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new PaymentHandleResponse(INTERNAL_SERVER_ERROR, errorCode, errorMessage, null);
    }

    @JsonIgnore
    public int getStatusCode() {
        return statusCode;
    }

    @Nullable
    public String getErrorCode() {
        return errorCode;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    @Nullable
    public String getApprovalUrl() {
        return approvalUrl;
    }

    /**
     * @return standard Spring {@link ResponseEntity} with <b><code>this</code></b> body and <i>statusCode</i> same as
     * {@link #statusCode}
     */
    public ResponseEntity<PaymentHandleResponse> toResponseEntity() {
        return new ResponseEntity<>(this, httpStatus);
    }
}
