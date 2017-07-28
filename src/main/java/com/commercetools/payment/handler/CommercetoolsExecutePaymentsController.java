package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

import static java.lang.String.format;

@RestController
public class CommercetoolsExecutePaymentsController extends BaseCommercetoolsPaymentsController {

    @Autowired
    public CommercetoolsExecutePaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                  @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/commercetools/execute/payments/{paypalPlusPaymentId}")
    public ResponseEntity<String> executePayments(@PathVariable String tenantName,
                                                 @PathVariable String paypalPlusPaymentId,
                                                 @RequestParam String paypalPlusPayerId) {
        PaymentHandleResult paymentHandleResult = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.handlePayment(paypalPlusPaymentId, paypalPlusPayerId))
                .orElseGet(() -> new PaymentHandleResult(HttpStatus.NOT_FOUND, format("Tenant [%s] not found", tenantName)));
        return new ResponseEntity<>(paymentHandleResult.getBody(), paymentHandleResult.getStatusCode());
    }

}
