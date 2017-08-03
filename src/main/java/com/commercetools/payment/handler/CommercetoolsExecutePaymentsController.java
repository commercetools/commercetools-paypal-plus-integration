package com.commercetools.payment.handler;

import com.commercetools.payment.handler.commandObject.PaypalPlusExecuteParams;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.validation.Valid;

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
            consumes = "application/json",
            produces = "application/json",
            value = "/{tenantName}/commercetools/execute/payments/")
    public ResponseEntity executePayments(@PathVariable String tenantName,
                                          @Valid @RequestBody PaypalPlusExecuteParams params,
                                          BindingResult result) {
        PaymentHandleResponse paymentHandleResponse = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.executePayment(params.getPaypalPlusPaymentId(), params.getPaypalPlusPayerId()))
                .orElseGet(() -> PaymentHandleResponse.of404NotFound(format("Tenant [%s] not found", tenantName)));
        return paymentHandleResponse.toResponseEntity();
    }

}
