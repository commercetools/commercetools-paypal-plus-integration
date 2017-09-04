package com.commercetools.service.paypalPlus.impl;

import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.Event;
import com.paypal.api.payments.EventType;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.Webhook;
import com.paypal.api.payments.WebhookList;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;

import javax.annotation.Nonnull;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <b>Note:</b> since {@link APIContext} caches <i>PayPal-Request-Id</i> one should not reuse the same instance of
 * {@link PaypalPlusPaymentServiceImpl} for multiple Paypal requests.
 */
public class PaypalPlusPaymentServiceImpl extends BasePaypalPlusService implements PaypalPlusPaymentService {

    public PaypalPlusPaymentServiceImpl(@Nonnull APIContextFactory paypalPlusApiContextFactory) {
        super(paypalPlusApiContextFactory);
    }

    @Override
    public CompletionStage<Payment> create(@Nonnull Payment payment) {
        return paymentStageWrapper(payment::create);
    }

    @Override
    public CompletionStage<Payment> patch(@Nonnull Payment payment, @Nonnull Patch patch) {
        return paymentStageWrapper(paypalPlusApiContext -> {
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
        return paymentStageWrapper(paypalPlusApiContext -> payment.execute(paypalPlusApiContext, paymentExecution));
    }

    @Override
    public CompletionStage<Payment> getByPaymentId(@Nonnull String paymentId) {
        return paymentStageWrapper(paypalPlusApiContext -> Payment.get(paypalPlusApiContext, paymentId));
    }

    @Override
    public CompletionStage<Webhook> createWebhook(@Nonnull String notificationUrl) {
        // https://github.com/paypal/PayPal-Java-SDK/blob/1f85723f3df153e6b876dc2325dcef464af8b6e4/rest-api-sdk/src/test/java/com/paypal/api/payments/WebhookTestCase.java#L105
        return paymentStageWrapper(EventType::availableEventTypes)
                .thenCompose(eventTypeList -> paymentStageWrapper(apiContext -> {
                    Webhook webhook = new Webhook();
                    webhook.setUrl(notificationUrl);
                    webhook.setEventTypes(eventTypeList.getEventTypes());
                    return webhook.create(apiContext, webhook);
                }));
    }

    @Override
    public CompletionStage<Webhook> ensureWebhook(@Nonnull String notificationUrl) {
        return paymentStageWrapper(apiContext -> {
            WebhookList webhookList = new WebhookList();
            webhookList = webhookList.getAll(apiContext);
            return webhookList.getWebhooks().stream()
                    .filter(webhook -> notificationUrl.equalsIgnoreCase(webhook.getUrl()))
                    .findAny();
        }).thenCompose(webhookOpt -> webhookOpt
                .map(webhook -> CompletableFuture.completedFuture(webhookOpt.get()))
                .orElseGet(() -> createWebhook(notificationUrl).toCompletableFuture())
        );
    }

    @Override
    public CompletionStage<Boolean> validateNotificationEvent(@Nonnull Webhook webhook,
                                                              @Nonnull Map<String, String> headersInfo,
                                                              @Nonnull String requestBody) {
        return paymentStageWrapper(apiContext -> {
            try {
                apiContext.addConfiguration(Constants.PAYPAL_WEBHOOK_ID, webhook.getId());
                return Event.validateReceivedEvent(apiContext, headersInfo, requestBody);
            } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
                // todo: handle the errors better
                throw new PayPalRESTException("Cannot validate notification event.");
            }
        });
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
                throw new PaypalPlusServiceException("Paypal Plus payment service exception", e);
            }
        });
    }

    @FunctionalInterface
    private interface PayPalRESTExceptionSupplier<T, R> {
        R apply(T apiContext) throws PayPalRESTException;
    }
}
