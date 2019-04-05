package com.commercetools.service.paypalPlus.impl;

import com.commercetools.exception.IntegrationServiceException;
import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.pspadapter.ExtendedAPIContextFactory;
import com.commercetools.pspadapter.util.ExtendedAPIContext;
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

    final ExtendedAPIContextFactory paypalPlusExtendedApiContextFactory;

    private final Logger logger;

    BasePaypalPlusService(@Nonnull ExtendedAPIContextFactory paypalPlusExtendedApiContextFactory) {
        this.paypalPlusExtendedApiContextFactory = paypalPlusExtendedApiContextFactory;
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
        ExtendedAPIContext extendedAPIContext = paypalPlusExtendedApiContextFactory.createAPIContext();
        String tenantName = extendedAPIContext.getTenantName();

        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.apply(extendedAPIContext.getApiContext());
            } catch (PayPalRESTException e) {
                throw new PaypalPlusServiceException("Paypal Plus REST service exception for tenant: " + tenantName, e);
            } catch (Throwable e) {
                throw new IntegrationServiceException("Paypal Plus REST service unexpected exception. For tenant: "  + tenantName, e);
            }
        });
    }

    @FunctionalInterface
    protected interface PayPalRESTExceptionSupplier<T, R> {
        R apply(T apiContext) throws PayPalRESTException, GeneralSecurityException;
    }
}
