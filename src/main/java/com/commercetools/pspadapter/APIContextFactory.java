package com.commercetools.pspadapter;

import com.paypal.base.rest.APIContext;
import io.sphere.sdk.models.Base;

/**
 * Helper class for {@link APIContext} as we cannot directly inject {@link APIContext} into
 * {@link com.commercetools.service.paypalPlus.PaypalPlusPaymentService},
 * but {@link APIContext} needs to be recreated for every Paypal Plus call.
 *
 * @see <a href="https://github.com/commercetools/commercetools-paypal-plus-integration/issues/27">
 *     Github issue</a>
 *
 */
public class APIContextFactory extends Base {

    private final String pPlusClientId;
    private final String pPlusClientSecret;
    private final String pPlusClientMode;

    public APIContextFactory(String pPlusClientId,
                             String pPlusClientSecret,
                             String pPlusClientMode) {
        this.pPlusClientId = pPlusClientId;
        this.pPlusClientSecret = pPlusClientSecret;
        this.pPlusClientMode = pPlusClientMode;
    }

    /**
     * @return new instance of {@link APIContext} with respective Paypal Plus clientId, clientSecret and mode.
     */
    public APIContext createAPIContext() {
        return new APIContext(this.pPlusClientId, this.pPlusClientSecret, this.pPlusClientMode);
    }
}