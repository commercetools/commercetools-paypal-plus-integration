package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.annotation.Nonnull;

public class BaseCommercetoolsPaymentsController {
    protected final StringTrimmerEditor stringTrimmerEditor;
    protected final PaymentHandlerProvider paymentHandlerProvider;

    public BaseCommercetoolsPaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                               @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        this.stringTrimmerEditor = stringTrimmerEditor;
        this.paymentHandlerProvider = paymentHandlerProvider;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
