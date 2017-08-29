package com.commercetools.service.paypalPlus.impl;

import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <b>Note:</b> since {@link APIContext} caches <i>PayPal-Request-Id</i> one should not reuse the same instance of
 * {@link PaypalPlusPaymentServiceImpl} for multiple Paypal requests.
 */
public class PaypalPlusPaymentServiceImpl extends BasePaypalPlusService implements PaypalPlusPaymentService {

    @Autowired
    public PaypalPlusPaymentServiceImpl(@Nonnull APIContextFactory paypalPlusApiContextFactory) {
        super(paypalPlusApiContextFactory);
    }

    @Override
    public CompletionStage<Payment> create(@Nonnull Payment payment) {
        return paymentStageWrapper((paypalPlusApiContext) -> payment.create(paypalPlusApiContext));
    }

    @Override
    public CompletionStage<Payment> patch(@Nonnull Payment payment, @Nonnull List<Patch> patches) {
        return paymentStageWrapper((paypalPlusApiContext) -> {
            payment.update(paypalPlusApiContext, patches);
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
        return paymentStageWrapper((paypalPlusApiContext) -> payment.execute(paypalPlusApiContext, paymentExecution));
    }

    @Override
    public CompletionStage<Payment> lookUp(@Nonnull String paymentId) {
        return paymentStageWrapper((paypalPlusApiContext) -> Payment.get(paypalPlusApiContext, paymentId));
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
    private <R> CompletionStage<R> paymentStageWrapper(@Nonnull PayPalRESTExceptionSupplier<APIContext, R> supplier)
            throws PaypalPlusServiceException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                APIContext context = paypalPlusApiContextFactory.createAPIContext();
                return supplier.apply(context);
            } catch (PayPalRESTException e) {
                throw new PaypalPlusServiceException("Create Paypal Plus payment exception", e);
            }
        });
    }

    @FunctionalInterface
    private interface PayPalRESTExceptionSupplier<T, R> {
        R apply(T apiContext) throws PayPalRESTException;
    }
}
