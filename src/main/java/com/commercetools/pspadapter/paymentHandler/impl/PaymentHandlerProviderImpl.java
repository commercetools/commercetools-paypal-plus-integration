package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.ShippingAddressMapper;
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
    private final PaymentMapper paymentMapper;
    private final ShippingAddressMapper shippingAddressMapper;
    private final Gson gson;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                      @Nonnull PaymentMapper paymentMapper,
                                      @Nonnull ShippingAddressMapper shippingAddressMapper,
                                      @Nonnull Gson gson) {
        this.configFactory = configFactory;
        this.paymentMapper = paymentMapper;
        this.shippingAddressMapper = shippingAddressMapper;
        this.gson = gson;
    }

    @Override
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        return configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();
                    PaypalPlusFacade payPalPlusFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();
                    return new PaymentHandler(ctpFacade, paymentMapper, shippingAddressMapper, payPalPlusFacade, tenantName, gson);
                });
    }

}