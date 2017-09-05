package com.commercetools.payment.handler;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

import javax.annotation.Nonnull;

public class BaseCommercetoolsPaymentsHandleController extends BaseCommercetoolsController {
    protected final PaymentHandlerProvider paymentHandlerProvider;

    public BaseCommercetoolsPaymentsHandleController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                     @Nonnull PaymentHandlerProvider paymentHandlerProvider) {
        super(stringTrimmerEditor);
        this.paymentHandlerProvider = paymentHandlerProvider;
    }
}
