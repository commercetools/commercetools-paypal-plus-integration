package com.commercetools.helper.mapper;

import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.Payment;

import javax.annotation.Nonnull;

public interface PaymentMapper {

    /**
     * See Chapter <b><code>04. Payments</code></b> of <i>Integrating PayPal PLUS</i> guide.
     *
     * @param ctpPaymentWithCart commercetools cart and payment to map
     * @return Paypal Plus {@link Payment} with respective values from {@link CtpPaymentWithCart}.
     * @see <a href="https://www.paypalobjects.com/webstatic/downloads/PayPal-PLUS-IntegrationGuide.pdf">
     * Integrating PayPal PLUS</a>
     */
    @Nonnull
    Payment ctpPaymentToPaypalPlus(@Nonnull CtpPaymentWithCart ctpPaymentWithCart);

}
