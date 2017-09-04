package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.annotation.Nonnull;

public class BaseCommercetoolsPaymentsController extends BaseCommercetoolsController {
    protected PaymentHandlerProvider paymentHandlerProvider;

    public BaseCommercetoolsPaymentsController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                               @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor);
        this.paymentHandlerProvider = paymentHandlerProvider;
    }
}
