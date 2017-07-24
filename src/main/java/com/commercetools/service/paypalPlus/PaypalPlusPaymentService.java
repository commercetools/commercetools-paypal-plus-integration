package com.commercetools.service.paypalPlus;

import com.commercetools.exception.PaypalPlusServiceException;
import com.paypal.api.payments.Payment;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

public interface PaypalPlusPaymentService {

    /**
     * Create a payment on the Paypal Plus environment.
     * <p>
     * If the request is failed - {@link PaypalPlusServiceException} is thrown inside the completion stage.
     *
     * @param payment {@link Payment} to save
     * @return {@link CompletionStage<Payment>} of the same <i>updated</i> payment.
     */
    CompletionStage<Payment> create(@Nonnull Payment payment);
}
