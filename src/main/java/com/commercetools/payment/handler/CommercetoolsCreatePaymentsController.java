package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

import static java.lang.String.format;

@RestController
public class CommercetoolsCreatePaymentsController extends BaseCommercetoolsPaymentsController {

    @Autowired
    public CommercetoolsCreatePaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                 @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/commercetools/create/payments/{ctpPaymentId}")
    public ResponseEntity<String> handlePayments(@PathVariable String tenantName,
                                                 @PathVariable String ctpPaymentId) {
        PaymentHandleResult paymentHandleResult = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.createPayment(ctpPaymentId))
                .orElseGet(() -> new PaymentHandleResult(HttpStatus.NOT_FOUND, format("Tenant [%s] not found", tenantName)));
        return new ResponseEntity<>(paymentHandleResult.getBody(), paymentHandleResult.getStatusCode());
    }


}
