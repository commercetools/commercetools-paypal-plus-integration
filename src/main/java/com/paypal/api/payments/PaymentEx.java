package com.paypal.api.payments;

import com.paypal.api.ApplicationContext;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Temporary extension of default PaypalPlus {@link Payment} class while the PayPal Plus SDK developers implement
 * {@link ApplicationContext application_context} entity for the payments. The class could be removed/re-factored when respective
 * Paypal Plus Java SDK issue is fixed.
 *
 * @see <a href="https://developer.paypal.com/docs/api/orders/#definition-application_context">Application Context</a>
 * @see <a href="https://github.com/paypal/PayPal-Java-SDK/issues/330">#330 Payment/Order#applicationContext property is not available</a>
 * @see ApplicationContext
 */
public class PaymentEx extends Payment {
    /**
     * Identifier of the payment resource created.
     */
    private ApplicationContext applicationContext;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public PaymentEx setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
