package com.commercetools.service.paypalPlus;

import com.commercetools.exception.PaypalPlusServiceException;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.Webhook;

import javax.annotation.Nonnull;
import java.util.Map;
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

    CompletionStage<Payment> patch(@Nonnull Payment payment, @Nonnull Patch patch);

    CompletionStage<Payment> execute(@Nonnull Payment payment, @Nonnull PaymentExecution paymentExecution);

    CompletionStage<Payment> getByPaymentId(@Nonnull String paymentId);

    CompletionStage<Webhook> createWebhook(@Nonnull String notificationUrl);

    /**
     * Check if webhook for the notification URL exists.
     * If not, create a new webhook on Paypal Plus.
     * If it exists, return existing one.
     *
     * @param notificationUrl notification URL
     * @return completion stage
     */
    CompletionStage<Webhook> ensureWebhook(@Nonnull String notificationUrl);

    /**
     * Validate if the notification event is the legitimate one from Paypal Plus.
     * @param webhook paypal plus webhook object
     * @param headersInfo headers from the current request
     * @param requestBody body of the current request
     * @return completion stage true if it's a legit notification
     */
    CompletionStage<Boolean> validateNotificationEvent(@Nonnull Webhook webhook,
                                                       @Nonnull Map<String, String> headersInfo,
                                                       @Nonnull String requestBody);
}
