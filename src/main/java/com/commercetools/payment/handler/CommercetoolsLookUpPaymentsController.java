package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CommercetoolsLookUpPaymentsController extends BaseCommercetoolsPaymentsHandleController {

    public CommercetoolsLookUpPaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                 @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @GetMapping(
            value = "/{tenantName}/commercetools/look-up/payments/{paypalPaymentId}",
            produces = APPLICATION_JSON_VALUE)
    public CompletionStage<ResponseEntity> lookUpPayment(@PathVariable String tenantName,
                                                         @PathVariable String paypalPaymentId) {
        return getTenantHandlerResponse(tenantName, paymentHandler -> paymentHandler.lookUpPayment(paypalPaymentId));
    }
}