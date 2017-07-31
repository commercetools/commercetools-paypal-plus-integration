package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.ShippingAddressMapper;
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

    private final TenantConfigFactory configFactory;
    private final PaymentMapper paymentMapper;
    private final ShippingAddressMapper shippingAddressMapper;

    @Autowired
    public PaymentHandlerProviderImpl(@Nonnull TenantConfigFactory configFactory,
                                      @Nonnull PaymentMapper paymentMapper,
                                      @Nonnull ShippingAddressMapper shippingAddressMapper) {
        this.configFactory = configFactory;
        this.paymentMapper = paymentMapper;
        this.shippingAddressMapper = shippingAddressMapper;
    }

    @Override
    public Optional<PaymentHandler> getPaymentHandler(@Nonnull String tenantName) {
        return configFactory.getTenantConfig(tenantName)
                .map(tenantConfig -> {
                    CtpFacade ctpFacade = CtpFacadeFactory.getCtpFacade(tenantConfig);
                    PaypalPlusFacade payPalPlusFacade = PaypalPlusFacadeFactory.getPaypalPlusFacade(tenantConfig);
                    return new PaymentHandler(ctpFacade, paymentMapper, shippingAddressMapper, payPalPlusFacade, tenantName);
                });
    }

}