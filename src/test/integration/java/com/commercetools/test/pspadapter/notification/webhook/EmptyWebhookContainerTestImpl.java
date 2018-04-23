package com.commercetools.test.pspadapter.notification.webhook;

import com.commercetools.context.annotation.PrimaryForTest;
import com.commercetools.pspadapter.notification.webhook.WebhookContainer;
import com.paypal.api.payments.Webhook;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * Empty default {@link WebhookContainer} implementation for the integration tests.
 */
@Component
@PrimaryForTest // for the tests skip webhooks registration
public class EmptyWebhookContainerTestImpl implements WebhookContainer {

    public EmptyWebhookContainerTestImpl() {
        //do nothing
        LoggerFactory.getLogger(this.getClass().getSimpleName()).info("Skip WebhookContainer initialization");
    }

    @Override
    public CompletionStage<Webhook> getWebhookCompletionStageByTenantName(@Nonnull String tenantName) {
        throw new NotImplementedException("com.commercetools.test.pspadapter.notification.webhook.WebhookContainerTestImpl.getWebhookCompletionStageByTenantName not implemented");
    }
}