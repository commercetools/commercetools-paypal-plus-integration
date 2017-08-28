package com.commercetools.service.paypalPlus.impl;

import com.commercetools.pspadapter.APIContextFactory;

import javax.annotation.Nonnull;

abstract class BasePaypalPlusService {

    final APIContextFactory paypalPlusApiContextFactory;

    BasePaypalPlusService(@Nonnull APIContextFactory paypalPlusApiContextFactory) {
        this.paypalPlusApiContextFactory = paypalPlusApiContextFactory;
    }
}
