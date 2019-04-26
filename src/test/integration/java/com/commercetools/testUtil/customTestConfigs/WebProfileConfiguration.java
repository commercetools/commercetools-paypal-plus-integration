package com.commercetools.testUtil.customTestConfigs;

import com.commercetools.pspadapter.ExtendedAPIContextFactory;
import com.paypal.api.payments.InputFields;
import com.paypal.api.payments.WebProfile;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.commercetools.testUtil.TestConstants.WEB_PROFILE_NO_ADDRESS_OVERRIDE;
import static java.util.Optional.ofNullable;

/**
 * Configuration to ensure web profile with {@code address_override == 1} exists.
 * <p>
 * After all this configuration injects {@link #noAddressOverrideWebProfile} bean.
 * <p>
 * <b>Note:</b> for now this payment integrator does not implement/expose web experience profile services/endpoints,
 * hence we implement this tests using paypal SDK directly. This might be changed in the future, if the web experience
 * profile services will be implemented byt this payment integrator.
 *
 * @see <a href="https://developer.paypal.com/docs/api/payment-experience/">Payment Experience Web Profiles API</a>
 */
public class WebProfileConfiguration {

    @Autowired
    private ExtendedAPIContextFactory paypalPlusExtendedApiContextFactory;

    private WebProfile _noAddressOverrideWebProfile;

    /**
     * @return Paypal Plus web experience profile instance from test (sandbox) project
     * with {@code address_override == 1}
     */
    @Bean
    private WebProfile noAddressOverrideWebProfile() {
        assert _noAddressOverrideWebProfile != null : "noAddressOverrideWebProfile bean is not initialized";
        return _noAddressOverrideWebProfile;
    }

    /**
     * Ensure test web profile with name {@link com.commercetools.testUtil.TestConstants#WEB_PROFILE_NO_ADDRESS_OVERRIDE}.
     * Creates the profile with the name and {@code address_override == 1} if doesn't exist. "Injects" respective
     * {@link #noAddressOverrideWebProfile} bean after initialization.
     * <p>
     * <b>Note:</b> since web profile service is not implemented by the application - we use directly PaypalPlus SDK
     * to get/create the profile.
     */
    @PostConstruct
    void init() throws PayPalRESTException {
        List<WebProfile> list = WebProfile.getList(paypalPlusExtendedApiContextFactory.createAPIContext().getApiContext());
        _noAddressOverrideWebProfile = ofNullable(list)
                .map(List::stream)
                .flatMap(stream -> stream
                        .filter(webProfile -> WEB_PROFILE_NO_ADDRESS_OVERRIDE.equals(webProfile.getName())).findFirst())
                .orElse(null);

        if (_noAddressOverrideWebProfile == null) {
            WebProfile webProfile = new WebProfile(WEB_PROFILE_NO_ADDRESS_OVERRIDE);
            InputFields inputFields = new InputFields();
            inputFields.setAddressOverride(1); // no address override allowed
            webProfile.setInputFields(inputFields);
            _noAddressOverrideWebProfile = webProfile.create(paypalPlusExtendedApiContextFactory.createAPIContext().getApiContext());
        }
    }


}
