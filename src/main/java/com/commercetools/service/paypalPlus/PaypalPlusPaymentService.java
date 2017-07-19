package com.commercetools.service.paypalPlus;

import com.commercetools.exception.PaypalPlusServiceException;
import com.paypal.api.payments.Payment;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

public interface PaypalPlusPaymentService {

    /**
     * @param payment
     * @return
     * @throws {@link PaypalPlusServiceException} unchecked exception when can't be created
     */
    CompletionStage<Payment> create(@Nonnull Payment payment);
}
