package com.commercetools.payment.handler;

import com.commercetools.payment.handler.commandObject.PaypalPlusExecuteParams;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CommercetoolsExecutePaymentsController extends BaseCommercetoolsPaymentsHandleController {

    @Autowired
    public CommercetoolsExecutePaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                  @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
            value = "/{tenantName}/commercetools/execute/payments")
    public CompletionStage<ResponseEntity> executePayments(@PathVariable String tenantName,
                                                           @Valid @RequestBody PaypalPlusExecuteParams params,
                                                           @Nonnull BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().stream()
                    .map(ObjectError::toString)
                    .collect(Collectors.joining(". "));
            return completedFuture(PaymentHandleResponse.of400BadRequest(errorMessage).toResponseEntity());
        }

        return getTenantHandlerResponse(tenantName,
                paymentHandler -> paymentHandler.executePayment(params.getPaypalPlusPaymentId(), params.getPaypalPlusPayerId()));
    }

}
