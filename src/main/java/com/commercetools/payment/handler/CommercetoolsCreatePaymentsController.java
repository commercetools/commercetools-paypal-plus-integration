package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CommercetoolsCreatePaymentsController extends BaseCommercetoolsPaymentsHandleController {

    @Autowired
    public CommercetoolsCreatePaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                 @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/commercetools/create/payments/{ctpPaymentId}",
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity createPayment(@PathVariable String tenantName,
                                        @PathVariable String ctpPaymentId) {
        PaymentHandleResponse paymentHandleResponse = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.createPayment(ctpPaymentId))
                .orElseGet(() -> PaymentHandleResponse.of404NotFound(format("Tenant [%s] not found", tenantName)));
        return paymentHandleResponse.toResponseEntity();
    }

}