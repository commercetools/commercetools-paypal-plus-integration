package com.commercetools.payment.handler;

import com.commercetools.payment.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
public class CommercetoolsHandlePaymentsController {

    private static final String template = "Hello, payment [%s]!";

    private final StringTrimmerEditor stringTrimmerEditor;

    @Autowired
    public CommercetoolsHandlePaymentsController(StringTrimmerEditor stringTrimmerEditor) {
        this.stringTrimmerEditor = stringTrimmerEditor;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/{tenantName}/commercetools/handle/payments/{paymentId}")
    public Payment handlePayments(@PathVariable String tenantName,
                                  @PathVariable String paymentId) {
        return new Payment(tenantName,
                String.format(template, paymentId));
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
