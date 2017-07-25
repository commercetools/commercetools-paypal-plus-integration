package com.commercetools.helper.mapper;

import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.Payment;

import javax.annotation.Nullable;

public interface PaymentMapper {

    /**
     * See Chapter <b><code>04. Payments</code></b> of <i>Integrating PayPal PLUS</i> guide.
     *
     * @param ctpPaymentWithCart
     * @return
     * @see <a href="https://www.paypalobjects.com/webstatic/downloads/PayPal-PLUS-IntegrationGuide.pdf">
     * Integrating PayPal PLUS</a>
     */
    @Nullable
    Payment ctpPaymentToPaypalPlus(@Nullable CtpPaymentWithCart ctpPaymentWithCart);

}
