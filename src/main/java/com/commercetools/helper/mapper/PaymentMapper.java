package com.commercetools.helper.mapper;

import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;

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
     * Get enum which maps from commercetools payment method names
     * (Payment#{@link io.sphere.sdk.payments.Payment#getPaymentMethodInfo() getPaymentMethodInfo}#{@link PaymentMethodInfo#getMethod() getMethod()})
     * to respective Paypal Plus payment method names.
     * <p>
     * If the mapping explicitly is not defined - some default mapping is expected.
     *
     * @return {@link CtpToPaypalPlusPaymentMethodsMapping} instance to map CTP payment name to Paypal Plus one.
     * If mapping is not defined explicitly - default mapping is expected.
     */
    @Nonnull
    CtpToPaypalPlusPaymentMethodsMapping getCtpToPpPaymentMethodsMapping();

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
