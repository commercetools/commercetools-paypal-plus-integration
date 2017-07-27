package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

public class PaypalPlusFacadeFactory {

    public static PaypalPlusFacade getPaypalPlusFacade(@Nonnull TenantConfig tenantConfig) {
        PaypalPlusPaymentServiceImpl service = new PaypalPlusPaymentServiceImpl(tenantConfig.createAPIContext());
        return new PaypalPlusFacade(service);
    }
}