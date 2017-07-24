package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.executor.CtpExecutor;
import com.commercetools.pspadapter.executor.CtpExecutorFactory;
import com.commercetools.pspadapter.executor.PaypalPlusExecutor;
import com.commercetools.pspadapter.executor.PaypalPlusExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class PaymentHandlerProviderImpl implements PaymentHandlerProvider {

    private final CtpExecutorFactory ctpFactory;
    private final PaypalPlusExecutorFactory pPPFactory;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull CtpExecutorFactory ctpFactory,
                                      @Nonnull PaypalPlusExecutorFactory pPPFactory) {
        this.ctpFactory = ctpFactory;
        this.pPPFactory = pPPFactory;
    }

    @Override
    public PaymentHandler getPaymentHandler(@Nonnull String tenantName) {
        CtpExecutor ctpExecutor = ctpFactory.getCtpExecutor(tenantName);
        PaypalPlusExecutor payPalPlusExecutor = pPPFactory.getPayPalPlusExecutor(tenantName);
        return new PaymentHandler(ctpExecutor, payPalPlusExecutor);
    }

}