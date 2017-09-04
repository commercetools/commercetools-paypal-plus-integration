package com.commercetools.pspadapter.notification.webhook;

import com.paypal.api.payments.Webhook;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

public interface WebhookContainer {

    CompletionStage<Webhook> getWebhookCompletionStageByTenantName(@Nonnull String tenantName);
}
