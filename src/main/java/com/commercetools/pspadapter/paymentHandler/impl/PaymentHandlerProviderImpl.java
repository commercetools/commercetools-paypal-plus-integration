package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class PaymentHandlerProviderImpl implements PaymentHandlerProvider {

    private final CtpFacadeFactory ctpFactory;
    private final PaypalPlusFacadeFactory pPPFactory;
    private final TenantConfigFactory config;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull CtpFacadeFactory ctpFactory,
                                      @Nonnull PaypalPlusFacadeFactory pPPFactory) {
        this.ctpFactory = ctpFactory;
        this.pPPFactory = pPPFactory;
    }

    @Override
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        return config.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpExecutor = ctpFactory.getCtpFacade(tenantConfig);
                    PaypalPlusFacade payPalPlusExecutor = pPPFactory.getPaypalPlusFacade(tenantConfig);
                    return new PaymentHandler(ctpExecutor, payPalPlusExecutor, tenantName);
                });
    }

}