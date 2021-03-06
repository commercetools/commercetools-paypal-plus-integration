package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.PaymentMapperHelper;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.paymentHandler.PaymentHandlerProvider;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

@Component
public class PaymentHandlerProviderImpl implements PaymentHandlerProvider {

    private final TenantConfigFactory configFactory;
    private final PaypalPlusFacadeFactory paypalPlusFacadeFactory;
    private final PaymentMapperHelper paymentMapperHelper;
    private final Gson paypalGson;
    private final PaypalPlusFormatter paypalPlusFormatter;
    private final CtpFacadeFactory ctpFacadeFactory;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                      @Nonnull PaypalPlusFacadeFactory paypalPlusFacadeFactory,
                                      @Nonnull PaymentMapperHelper paymentMapperHelper,
                                      @Nonnull Gson paypalGson,
                                      @Nonnull PaypalPlusFormatter paypalPlusFormatter,
                                      @Nonnull CtpFacadeFactory ctpFacadeFactory) {
        this.configFactory = configFactory;
        this.paypalPlusFacadeFactory = paypalPlusFacadeFactory;
        this.paymentMapperHelper = paymentMapperHelper;
        this.paypalGson = paypalGson;
        this.paypalPlusFormatter = paypalPlusFormatter;
        this.ctpFacadeFactory = ctpFacadeFactory;
    }

    @Override
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        return configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = ctpFacadeFactory.getCtpFacade(tenantConfig);
                    PaypalPlusFacade payPalPlusFacade = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig);
                    return new PaymentHandler(ctpFacade, paymentMapperHelper, payPalPlusFacade, tenantName, paypalGson, paypalPlusFormatter);
                });
    }

}