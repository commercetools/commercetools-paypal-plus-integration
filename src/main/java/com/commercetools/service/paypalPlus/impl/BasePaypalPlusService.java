package com.commercetools.service.paypalPlus.impl;

import com.paypal.base.rest.APIContext;

import javax.annotation.Nonnull;

abstract class BasePaypalPlusService {

    final APIContext paypalPlusApiContext;

    BasePaypalPlusService(@Nonnull APIContext paypalPlusApiContext) {
        this.paypalPlusApiContext = paypalPlusApiContext;
    }
}
