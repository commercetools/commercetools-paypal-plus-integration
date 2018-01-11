package com.commercetools.service.paypalPlus.impl;

import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.pspadapter.APIContextFactory;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.WebProfile;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.HttpMethod;
import com.paypal.base.rest.PayPalRESTException;
import com.paypal.base.rest.PayPalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

abstract class BasePaypalPlusService {

    final APIContextFactory paypalPlusApiContextFactory;

    private final Logger logger;

    BasePaypalPlusService(@Nonnull APIContextFactory paypalPlusApiContextFactory) {
        this.paypalPlusApiContextFactory = paypalPlusApiContextFactory;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    protected Logger getLogger() {
        return logger;
    }

    /**
     * This method is implemented to cover 2 issues of the default Paypal Plus service implementation:
     * <ol>
     * <li>{@link PayPalResource#configureAndExecute(APIContext, HttpMethod, String, String, Class, String)}
     * action is a blocking operation, but we want to have it asynchronous ({@link CompletionStage<R>}</li>
     * <li>{@link PayPalResource#configureAndExecute(APIContext, HttpMethod, String, String, Class, String)}
     * action throws <b>checked</b> {@link PayPalRESTException}, and this prevents us to use default completion
     * stage chains. So we wrap the exception to <i>unchecked</i> {@link PaypalPlusServiceException}</li>
     * </ol>
     *
     * @param supplier which call the necessary functions.
     * @param <R>      type of Paypal Plus entity ({@link Payment}, {@link WebProfile} etc)
     * @return a {@link CompletionStage<R>} with new stored Paypal Plus payment.
     * @throws PaypalPlusServiceException if {@link PayPalRESTException} fired - catch and re-throw wrapped exception.
     *                                    In case of any other exception - log the error and throw exception referring
     *                                    to the logs.
     */
    protected final <R> CompletionStage<R> paypalPlusStageWrapper(
            @Nonnull PayPalRESTExceptionSupplier<APIContext, R> supplier)
            throws PaypalPlusServiceException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                APIContext context = paypalPlusApiContextFactory.createAPIContext();
                return supplier.apply(context);
            } catch (PayPalRESTException e) {
                throw new PaypalPlusServiceException("Paypal Plus REST service exception", e);
            } catch (Throwable e) {
                logger.error("Paypal Plus REST service unexpected exception. ", e);
                throw new PaypalPlusServiceException("Paypal Plus REST service unexpected exception, see the logs");
            }
        });
    }

    @FunctionalInterface
    protected interface PayPalRESTExceptionSupplier<T, R> {
        R apply(T apiContext) throws PayPalRESTException, GeneralSecurityException;
    }
}
