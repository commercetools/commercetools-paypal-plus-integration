package com.commercetools.payment.handler;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.annotation.Nonnull;

public class BaseCommercetoolsController {
    protected final StringTrimmerEditor stringTrimmerEditor;

    public BaseCommercetoolsController(@Nonnull StringTrimmerEditor stringTrimmerEditor) {
        this.stringTrimmerEditor = stringTrimmerEditor;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, stringTrimmerEditor);
    }
}
