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

    CompletionStage<Payment> lookUp(@Nonnull String paymentId);

    CompletionStage<Webhook> createWebhook(@Nonnull String notificationUrl);

    CompletionStage<Webhook> ensureWebhook(@Nonnull String notificationUrl);

    CompletionStage<Boolean> validateNotificationEvent(@Nonnull Webhook webhook,
                                                       @Nonnull Map<String, String> headersInfo,
                                                       @Nonnull String requestBody);
}
