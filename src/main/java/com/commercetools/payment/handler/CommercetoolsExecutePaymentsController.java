package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            value = "/{tenantName}/commercetools/execute/payments/")
    public ResponseEntity executePayments(@PathVariable String tenantName,
                                                 @RequestParam String paypalPlusPaymentId,
                                                 @RequestParam String paypalPlusPayerId) {
        PaymentHandleResponse paymentHandleResponse = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.executePayment(paypalPlusPaymentId, paypalPlusPayerId))
                .orElseGet(() -> PaymentHandleResponse.of404NotFound(format("Tenant [%s] not found", tenantName)));
        return paymentHandleResponse.toResponseEntity();
    }

}
