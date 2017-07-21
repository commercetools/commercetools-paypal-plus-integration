package com.commercetools.service.main.impl;

import com.commercetools.service.main.PaymentHandlerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentHandlerProviderImpl implements PaymentHandlerProvider {

    private final CtpExecutorFactory ctpFactory;
    private final PaypalPlusExecutorFactory pPPFactory;

    @Autowired
    public PaymentHandlerProviderImpl(CtpExecutorFactory ctpFactory,
                                      PaypalPlusExecutorFactory pPPFactory) {
        this.ctpFactory = ctpFactory;
        this.pPPFactory = pPPFactory;
    }

    @Override
    public PaymentHandler getPaymentHandler(String tenantName) {
        CtpExecutor ctpExecutor = ctpFactory.getCtpExecutor(tenantName);
        PaypalPlusExecutor payPalPlusExecutor = pPPFactory.getPayPalPlusExecutor(tenantName);
        return new PaymentHandler(ctpExecutor, payPalPlusExecutor);
    }

}