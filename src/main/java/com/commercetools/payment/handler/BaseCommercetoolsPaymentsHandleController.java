package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class BaseCommercetoolsPaymentsHandleController extends BaseCommercetoolsController {
    protected final PaymentHandlerProvider paymentHandlerProvider;

    public BaseCommercetoolsPaymentsHandleController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                     @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor);
        this.paymentHandlerProvider = paymentHandlerProvider;
    }

    protected CompletionStage<ResponseEntity> getTenantHandlerResponse(@Nonnull String tenantName,
                                                                       @Nonnull Function<PaymentHandler, CompletionStage<PaymentHandleResponse>> paymentHandlerCaller) {
        return paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandlerCaller)
                .orElseGet(() -> completedFuture(PaymentHandleResponse.of404NotFound(format("Tenant [%s] not found", tenantName))))
                .thenApply(PaymentHandleResponse::toResponseEntity);
    }
}
