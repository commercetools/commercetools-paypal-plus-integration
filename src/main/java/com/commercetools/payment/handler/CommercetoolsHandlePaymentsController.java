package com.commercetools.payment.handler;

import com.commercetools.payment.PaymentDemo;
import com.commercetools.service.main.PaymentHandler;
import com.commercetools.service.main.PaymentHandlerCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommercetoolsHandlePaymentsController {

    private static final String template = "Hello, payment [%s]!";

    private final StringTrimmerEditor stringTrimmerEditor;
    private final PaymentHandlerCache paymentHandlerCache;

    @Autowired
    public CommercetoolsHandlePaymentsController(StringTrimmerEditor stringTrimmerEditor,
                                                 PaymentHandlerCache paymentHandlerCache) {
        this.stringTrimmerEditor = stringTrimmerEditor;
        this.paymentHandlerCache = paymentHandlerCache;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{tenantName}/commercetools/handle/payments/{paymentId}")
    public PaymentDemo handlePayments(@PathVariable String tenantName,
                                      @PathVariable String paymentId) {
        PaymentHandler paymentHandler = paymentHandlerCache.getPaymentHandler(tenantName);
        paymentHandler.handlePayment();
        // skeleton for tests: just "reflect" the tenant name and payment ID as JSON
        return new PaymentDemo(tenantName,
                String.format(template, paymentId));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
