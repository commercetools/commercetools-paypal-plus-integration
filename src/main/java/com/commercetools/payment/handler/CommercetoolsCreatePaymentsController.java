package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.web.bind.annotation.PostJsonResponseMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

@RestController
public class CommercetoolsCreatePaymentsController extends BaseCommercetoolsPaymentsHandleController {

    @Autowired
    public CommercetoolsCreatePaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                 @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor, paymentHandlerProvider);
    }

    @PostJsonResponseMapping(value = "/{tenantName}/commercetools/create/payments/{ctpPaymentId}")
    public CompletionStage<ResponseEntity> createPayment(@PathVariable String tenantName,
                                                         @PathVariable String ctpPaymentId) {
        return getTenantHandlerResponse(tenantName,
                paymentHandler -> paymentHandler.createPayment(ctpPaymentId));
    }

}