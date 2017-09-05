package com.commercetools.pspadapter.notification.webhook;

import com.paypal.api.payments.Webhook;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

public interface WebhookContainer {

    /**
     * Get Paypal's {@link Webhook} by the tenant's name. If none is found, returns {@code null}.
     * @param tenantName name of the tenant
     * @return tenant's {@link Webhook}. If none is found, {@code null} is returned.
     */
    CompletionStage<Webhook> getWebhookCompletionStageByTenantName(@Nonnull String tenantName);
}
