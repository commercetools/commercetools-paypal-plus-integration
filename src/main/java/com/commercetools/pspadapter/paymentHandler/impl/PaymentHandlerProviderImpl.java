package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.executor.CtpExecutor;
import com.commercetools.pspadapter.executor.CtpExecutorFactory;
import com.commercetools.pspadapter.executor.PaypalPlusExecutor;
import com.commercetools.pspadapter.executor.PaypalPlusExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

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
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        Optional<CtpExecutor> ctpExecutorOpt = ctpFactory.getCtpExecutor(tenantName);
        Optional<PaypalPlusExecutor> payPalPlusExecutorOpt = pPPFactory.getPayPalPlusExecutor(tenantName);
        if (ctpExecutorOpt.isPresent() && payPalPlusExecutorOpt.isPresent()) {
            CtpExecutor ctpExecutor = ctpExecutorOpt.get();
            PaypalPlusExecutor paypalPlusExecutor = payPalPlusExecutorOpt.get();
            return Optional.of(new PaymentHandler(ctpExecutor, paypalPlusExecutor));
        } else {
            return Optional.empty();
        }
    }

}