package com.commercetools.payment.constants.paypalPlus;

import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;

public final class PaypalPlusPaymentInterfaceName {

    /**
     * Value for PSP name, used in <code>
     * {@link Payment#getPaymentMethodInfo()}#{@link PaymentMethodInfo#getPaymentInterface()}
     * </code>
     */
    public static final String PAYPAL_PLUS = "PAYPAL_PLUS";

    private PaypalPlusPaymentInterfaceName() {
    }
}
