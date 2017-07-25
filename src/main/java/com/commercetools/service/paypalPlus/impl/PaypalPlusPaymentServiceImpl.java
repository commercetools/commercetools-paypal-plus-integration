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
        return createPaymentStage(payment);
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
     * @param preparedPayment Paypal Plus payment to store.
     * @return a {@link CompletionStage<Payment>} with new stored Paypal Plus payment.
     */
    private CompletionStage<Payment> createPaymentStage(@Nonnull Payment preparedPayment)
            throws PaypalPlusServiceException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return preparedPayment.create(paypalPlusApiContext);
            } catch (PayPalRESTException e) {
                throw new PaypalPlusServiceException("Create Paypal Plus payment exception", e);
            }
        });
    }
}
