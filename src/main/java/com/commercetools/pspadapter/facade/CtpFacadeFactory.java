package com.commercetools.pspadapter.facade;

import com.commercetools.pspadapter.tenant.TenantConfig;

import javax.annotation.Nonnull;

public interface CtpFacadeFactory {

    CtpFacade getCtpFacade(@Nonnull TenantConfig tenantConfig);
}