package com.commercetools.service.paypalPlus.impl;

import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <b>Note:</b> since {@link APIContext} caches <i>PayPal-Request-Id</i> one should not reuse the same instance of
 * {@link PaypalPlusPaymentServiceImpl} for multiple Paypal requests.
 */
public class PaypalPlusPaymentServiceImpl extends BasePaypalPlusService implements PaypalPlusPaymentService {

    @Autowired
    public PaypalPlusPaymentServiceImpl(@Nonnull APIContext paypalPlusApiContext) {
        super(paypalPlusApiContext);
    }

    @Override
    public CompletionStage<Payment> create(@Nonnull Payment payment) {
        return paymentStageWrapper(() -> payment.create(paypalPlusApiContext));
    }

    @Override
    public CompletionStage<Payment> patch(@Nonnull Payment payment, @Nonnull Patch patch) {
        return paymentStageWrapper(() -> {
            payment.update(paypalPlusApiContext, Collections.singletonList(patch));
            return payment;
        });
    }

    /**
     * @param payment
     * @param paymentExecution
     * @return
     */
    @Override
    public CompletionStage<Payment> execute(@Nonnull Payment payment, @Nonnull PaymentExecution paymentExecution) {
        return paymentStageWrapper(() -> payment.execute(paypalPlusApiContext, paymentExecution));
    }

    @Override
    public CompletionStage<Payment> lookUp(@Nonnull Payment payment) {
        return paymentStageWrapper(() -> Payment.get(paypalPlusApiContext, payment.getId()));
    }

    /**
     * This method is implemented to cover 2 issues of the default Paypal Plus service implementation:
     * <ol>
     * <li>{@link com.paypal.api.payments.Payment#create(com.paypal.base.rest.APIContext)} is blocking operation,
     * but we want to have it asynchronous ({@link CompletionStage<Payment>}</li>
     * <li>{@link com.paypal.api.payments.Payment#create(com.paypal.base.rest.APIContext)} throws <b>checked</b>
     * {@link PayPalRESTException}, and this prevents us to use default completion stage chains. So we wrap the
     * exception to <i>unchecked</i> {@link PaypalPlusServiceException}</li>
     * </ol>
     *
     * @param supplier which call the necessary functions.
     * @return a {@link CompletionStage<Payment>} with new stored Paypal Plus payment.
     */
    private <R> CompletionStage<R> paymentStageWrapper(@Nonnull PayPalRESTExceptionSupplier<R> supplier)
            throws PaypalPlusServiceException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.apply();
            } catch (PayPalRESTException e) {
                throw new PaypalPlusServiceException("Create Paypal Plus payment exception", e);
            }
        });
    }

    @FunctionalInterface
    private interface PayPalRESTExceptionSupplier<R> {
        R apply() throws PayPalRESTException;
    }
}
