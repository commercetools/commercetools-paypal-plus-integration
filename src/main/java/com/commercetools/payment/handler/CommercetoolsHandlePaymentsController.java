package com.commercetools.payment.handler;

import com.commercetools.payment.PaymentDemo;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.Optional;

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
    public PaymentDemo handlePayments(@PathVariable String tenantName,
                                      @PathVariable String paymentId) {
        Optional<PaymentHandler> paymentHandlerOpt = paymentHandlerProvider.getPaymentHandler(tenantName);
        paymentHandlerOpt.ifPresent(paymentHandler -> paymentHandler.handlePayment(paymentId));
        // skeleton for tests: just "reflect" the tenant name and payment ID as JSON
        return new PaymentDemo(tenantName,
                String.format(template, paymentId));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
