package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class PaymentHandlerProviderImpl implements PaymentHandlerProvider {

    private static final Logger logger = LoggerFactory.getLogger(PaymentHandlerProviderImpl.class);

    private final CtpFacadeFactory ctpFactory;
    private final PaypalPlusFacadeFactory pPPFactory;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull CtpFacadeFactory ctpFactory,
                                      @Nonnull PaypalPlusFacadeFactory pPPFactory) {
        this.ctpFactory = ctpFactory;
        this.pPPFactory = pPPFactory;
    }

    @Override
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        Optional<CtpFacade> ctpExecutorOpt = ctpFactory.getCtpFacade(tenantName);
        Optional<PaypalPlusFacade> payPalPlusExecutorOpt = pPPFactory.getPaypalPlusFacade(tenantName);
        if (ctpExecutorOpt.isPresent() && payPalPlusExecutorOpt.isPresent()) {
            CtpFacade ctpFacade = ctpExecutorOpt.get();
            PaypalPlusFacade paypalPlusFacade = payPalPlusExecutorOpt.get();
            return Optional.of(new PaymentHandler(ctpFacade, paypalPlusFacade, tenantName));
        } else {
            logger.warn("No tenants found for {}", tenantName);
            return Optional.empty();
        }
    }

}