package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

@RestController
public class CommercetoolsHandlePaymentsController {

    private static final String template = "Hello, payment [%s]!";

    private final StringTrimmerEditor stringTrimmerEditor;
    private final PaymentHandlerProvider paymentHandlerProvider;

    @Autowired
    public CommercetoolsHandlePaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                 @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        this.stringTrimmerEditor = stringTrimmerEditor;
        this.paymentHandlerProvider = paymentHandlerProvider;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{tenantName}/commercetools/handle/payments/{paymentId}")
    public ResponseEntity<?> handlePayments(@PathVariable String tenantName,
                                            @PathVariable String paymentId) {
        PaymentHandleResult paymentHandleResult = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.handlePayment(paymentId))
                .orElseGet(() -> new PaymentHandleResult(HttpStatus.NOT_FOUND, "Tenant " + tenantName + " not found"));
        return new ResponseEntity<>(paymentHandleResult.statusCode());
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
