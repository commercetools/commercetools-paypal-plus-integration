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
public class CommercetoolsPatchPaymentsController extends BaseCommercetoolsPaymentsController {

    @Autowired
    public CommercetoolsPatchPaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/commercetools/patch/payments/{ctpPaymentId}",
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity patchPayment(@PathVariable String tenantName,
                                        @PathVariable String ctpPaymentId) {
        PaymentHandleResponse paymentHandleResponse = paymentHandlerProvider
                .getPaymentHandler(tenantName)
                .map(paymentHandler -> paymentHandler.patchAddress(ctpPaymentId))
                .orElseGet(() -> PaymentHandleResponse.of404NotFound(format("Tenant [%s] not found", tenantName)));
        return paymentHandleResponse.toResponseEntity();
    }

}