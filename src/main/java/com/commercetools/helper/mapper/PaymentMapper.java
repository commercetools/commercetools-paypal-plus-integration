package com.commercetools.helper.mapper;

import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentLinkRel.APPROVAL_URL;
import static java.util.Optional.ofNullable;

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

    /**
     * TODO: not tested ;(
     * TODO: likely should be somewhere in the utils...
     *
     * @param payment
     * @return
     */
    static Optional<String> getApprovalUrl(@Nullable Payment payment) {
        return ofNullable(payment)
                .map(Payment::getLinks)
                .flatMap(links -> links.stream()
                        .filter(link -> APPROVAL_URL.equals(link.getRel()))
                        .map(Links::getHref)
                        .findFirst());
    }

}
