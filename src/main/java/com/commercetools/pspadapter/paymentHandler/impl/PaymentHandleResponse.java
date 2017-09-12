package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.config.ApplicationConfiguration;
import com.commercetools.payment.handler.CommercetoolsCreatePaymentsController;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.paypal.api.payments.Payment;
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

    /**
     * Not included to JSON response (so far).
     */
    private transient final HttpStatus httpStatus;

    private final String errorCode;

    private final String errorMessage;

    private final String approvalUrl;

    /**
     * @see #getPayment()
     */
    private final Payment payment;

    private PaymentHandleResponse(@Nonnull HttpStatus httpStatus, @Nullable String errorCode,
                                  @Nullable String errorMessage, @Nullable String approvalUrl,
                                  @Nullable Payment payment) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.approvalUrl = approvalUrl;
        this.payment = payment;
    }

    private PaymentHandleResponse(@Nonnull HttpStatus httpStatus,
                                  @Nullable String errorCode,
                                  @Nullable String errorMessage,
                                  @Nullable String approvalUrl) {
        this(httpStatus, errorCode, errorMessage, approvalUrl, null);
    }

    private PaymentHandleResponse(HttpStatus httpStatus, String errorCode, String errorMessage) {
        this(httpStatus, errorCode, errorMessage, null, null);
    }

    public static PaymentHandleResponse ofHttpStatus(HttpStatus httpStatus) {
        return new PaymentHandleResponse(httpStatus, null, null, null);
    }

    public static PaymentHandleResponse ofHttpStatusAndErrorMessage(HttpStatus httpStatus, String errorMessage) {
        return new PaymentHandleResponse(httpStatus, null, errorMessage);
    }

    public static PaymentHandleResponse of201CreatedApprovalUrl(@Nonnull String approvalUrl) {
        return new PaymentHandleResponse(CREATED, null, null, approvalUrl);
    }

    public static PaymentHandleResponse of200OkResponseBody(@Nonnull Payment responseBody) {
        return new PaymentHandleResponse(OK, null, null, null, responseBody);
    }

    public static PaymentHandleResponse of400BadRequest(@Nonnull String errorMessage) {
        return new PaymentHandleResponse(BAD_REQUEST, null, errorMessage);
    }

    public static PaymentHandleResponse of400BadRequest(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new PaymentHandleResponse(BAD_REQUEST, errorCode, errorMessage);
    }

    public static PaymentHandleResponse of404NotFound(@Nonnull String errorMessage) {
        return new PaymentHandleResponse(NOT_FOUND, null, errorMessage);
    }

    public static PaymentHandleResponse of404NotFound(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new PaymentHandleResponse(NOT_FOUND, errorCode, errorMessage);
    }

    public static PaymentHandleResponse of500InternalServerError(@Nonnull String errorMessage) {
        return new PaymentHandleResponse(INTERNAL_SERVER_ERROR, null, errorMessage);
    }

    public static PaymentHandleResponse of500InternalServerError(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new PaymentHandleResponse(INTERNAL_SERVER_ERROR, errorCode, errorMessage);
    }

    /**
     * Used mostly for tests.
     *
     * @return integer value of {@link #httpStatus}
     */
    @JsonIgnore
    public int getStatusCode() {
        return httpStatus.value();
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
     * <b>Note: {@link Payment} object has some deprecated getters (like {@link Payment#getClientCredential()}) which
     * cause a NPE if try to map them to JSON using default Spring Jackson mapper (which maps by getters). That's why
     * it is important to be sure the application uses extended JSON mapper, like
     * {@link ApplicationConfiguration#gson()}. For this reason we re-define default
     * {@link org.springframework.http.converter.json.GsonHttpMessageConverter} in the application context.</b>
     */
    @Nullable
    public Payment getPayment() {
        return payment;
    }

    /**
     * @return standard Spring {@link ResponseEntity} with <b><code>this</code></b> body and <i>statusCode</i> same as
     * {@link #httpStatus}
     */
    public ResponseEntity<PaymentHandleResponse> toResponseEntity() {
        return new ResponseEntity<>(this, httpStatus);
    }
}
