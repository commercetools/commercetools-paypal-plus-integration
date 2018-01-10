package com.commercetools.service.paypalPlus.impl;

import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.*;
import com.paypal.base.Constants;
import com.paypal.base.rest.APIContext;

import javax.annotation.Nonnull;
import java.util.List;
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
        return paypalPlusStageWrapper(payment::create);
    }

    @Override
    public CompletionStage<Payment> patch(@Nonnull Payment payment, @Nonnull List<Patch> patches) {
        return paypalPlusStageWrapper(paypalPlusApiContext -> {
            payment.update(paypalPlusApiContext, patches);
            return payment;
        });
    }

    @Override
    public CompletionStage<Payment> execute(@Nonnull Payment payment, @Nonnull PaymentExecution paymentExecution) {
        return paypalPlusStageWrapper(paypalPlusApiContext -> payment.execute(paypalPlusApiContext, paymentExecution));
    }

    @Override
    public CompletionStage<Payment> getByPaymentId(@Nonnull String paymentId) {
        return paypalPlusStageWrapper(paypalPlusApiContext -> Payment.get(paypalPlusApiContext, paymentId));
    }

    @Override
    public CompletionStage<Webhook> createWebhook(@Nonnull String notificationUrl) {
        // https://github.com/paypal/PayPal-Java-SDK/blob/1f85723f3df153e6b876dc2325dcef464af8b6e4/rest-api-sdk/src/test/java/com/paypal/api/payments/WebhookTestCase.java#L105
        return paypalPlusStageWrapper(EventType::availableEventTypes)
                .thenCompose(eventTypeList -> paypalPlusStageWrapper(apiContext -> {
                    Webhook webhook = new Webhook();
                    webhook.setUrl(notificationUrl);
                    webhook.setEventTypes(eventTypeList.getEventTypes());
                    return webhook.create(apiContext, webhook);
                }));
    }

    @Override
    public CompletionStage<Webhook> ensureWebhook(@Nonnull String notificationUrl) {
        return paypalPlusStageWrapper(apiContext -> {
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
        return paypalPlusStageWrapper(apiContext -> {
            apiContext.addConfiguration(Constants.PAYPAL_WEBHOOK_ID, webhook.getId());
            return Event.validateReceivedEvent(apiContext, headersInfo, requestBody);
        });
    }


}
