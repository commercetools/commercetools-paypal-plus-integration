package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.helper.mapper.AddressMapper;
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
    private final PaymentMapperHelper paymentMapperHelper;
    private final AddressMapper addressMapper;
    private final Gson gson;
    private final PaypalPlusFacadeFactory paypalPlusFacadeFactory;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                      @Nonnull PaypalPlusFacadeFactory paypalPlusFacadeFactory,
                                      @Nonnull PaymentMapperHelper paymentMapperHelper,
                                      @Nonnull AddressMapper addressMapper,
                                      @Nonnull Gson gson) {
        this.configFactory = configFactory;
        this.paypalPlusFacadeFactory = paypalPlusFacadeFactory;
        this.paymentMapperHelper = paymentMapperHelper;
        this.addressMapper = addressMapper;
        this.gson = gson;
    }

    @Override
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        return configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = new CtpFacadeFactory(tenantConfig).getCtpFacade();
                    PaypalPlusFacade payPalPlusFacade = paypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig);
                    return new PaymentHandler(ctpFacade, paymentMapperHelper, addressMapper, payPalPlusFacade, tenantName, gson);
                });
    }

}