package com.paypal.api;

import com.paypal.base.rest.PayPalResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Temporary {@code application_context} entity implementation for {@link com.paypal.api.payments.PaymentEx PaymentEx}.
 * Same as that class, this one should be removed/re-factored as soon as respective SDK issue is fixed.
 * <p>
 * <b>Note:</b> since right now we are interested only in one property {@code shippingPreference} -
 * we have implemented only this one, but there are more properties available:
 * <a href="https://developer.paypal.com/docs/api/orders/#definition-application_context">Application Context</a>
 *
 * @see <a href="https://developer.paypal.com/docs/api/orders/#definition-application_context">Application Context</a>
 * @see <a href="https://github.com/paypal/PayPal-Java-SDK/issues/330">#330 Payment/Order#applicationContext property is not available</a>
 * @see com.paypal.api.payments.PaymentEx PaymentEx
 */
public class ApplicationContext extends PayPalResource {

    /**
     * Payment shipping preference:<ul>
     * <li>NO_SHIPPING</li>
     * <li>GET_FROM_FILE</li>
     * <li>SET_PROVIDED_ADDRESS</li>
     * </ul>
     * Use {@code shippingPreference} enum custom field for the values.
     *
     * @see <a href="https://developer.paypal.com/docs/api/orders/#definition-application_context">application_context#shipping_preference</a>
     */
    private String shippingPreference;

    public String getShippingPreference() {
        return shippingPreference;
    }

    public ApplicationContext setShippingPreference(String shippingPreference) {
        this.shippingPreference = shippingPreference;
        return this;
    }

    // Paypal Plus SDK uses Lombok for equals/hashCode, but we have to use custom approach

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
