package com.commercetools.service.paypalPlus.impl;

import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PaypalPlusPaymentServiceImpl extends BasePaypalPlusService implements PaypalPlusPaymentService {

    @Autowired
    public PaypalPlusPaymentServiceImpl(@Nonnull APIContext paypalPlusApiContext) {
        super(paypalPlusApiContext);
    }

    @Override
    public CompletionStage<Payment> create(@Nonnull Payment payment) {
        return createPaymentStage(payment);
    }

    private CompletionStage<Payment> createPaymentStage(@Nonnull Payment preparedPayment) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return preparedPayment.create(paypalPlusApiContext);
            } catch (PayPalRESTException e) {
                throw new PaypalPlusServiceException("Create Paypal Plus payment exception", e);
            }
        });
    }
}
