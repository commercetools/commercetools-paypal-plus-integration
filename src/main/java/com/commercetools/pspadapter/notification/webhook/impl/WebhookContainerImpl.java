package com.commercetools.pspadapter.notification.webhook.impl;

import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.webhook.WebhookContainer;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.paypal.api.payments.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;
import static io.sphere.sdk.utils.CompletableFutureUtils.listOfFuturesToFutureOfList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class WebhookContainerImpl implements WebhookContainer {

    private CompletableFuture<Map<String, Webhook>> tenantNameToWebhookCompletionStage;

    @Autowired
    public WebhookContainerImpl(@Nonnull List<TenantConfig> tenantConfigs,
                                @Value("${ctp.paypal.plus.integration.server.url}") String integrationServerUrl) {
        init(tenantConfigs, integrationServerUrl);
    }

    private void init(@Nonnull List<TenantConfig> tenantConfigs,
                      @Nonnull String integrationServerUrl) {
        // create all necessary webhooks. This will certainly takes a while because it involves Paypal calls,
        // but without the webhooks configured correctly, the app cannot work properly

        List<CompletionStage<WebhookWithTenantName>> webhookStages = tenantConfigs.stream()
                .map(tenantConfig -> {
                    PaypalPlusFacade paypalPlusFacade = new PaypalPlusFacadeFactory(tenantConfig).getPaypalPlusFacade();
                    return paypalPlusFacade.getPaymentService()
                            .ensureWebhook(format("%s/%s/%s", integrationServerUrl, tenantConfig.getCtpProjectKey(), NOTIFICATION_PATH_URL))
                            .thenApply(webhook -> new WebhookWithTenantName(webhook, tenantConfig.getTenantName()));
                })
                .collect(toList());

        this.tenantNameToWebhookCompletionStage = listOfFuturesToFutureOfList(webhookStages)
                .thenApply(webhooks -> webhooks.stream()
                        .collect(toMap(WebhookWithTenantName::getTenantName, WebhookWithTenantName::getWebhook)));
    }

    public CompletionStage<Webhook> getWebhookCompletionStageByTenantName(@Nonnull String tenantName) {
        return this.tenantNameToWebhookCompletionStage
                .thenApply(webhookMap -> webhookMap.get(tenantName));
    }

    class WebhookWithTenantName {
        private Webhook webhook;
        private String tenantName;

        public WebhookWithTenantName(@Nonnull Webhook webhook,
                                     @Nonnull String tenantName) {
            this.webhook = webhook;
            this.tenantName = tenantName;
        }

        public Webhook getWebhook() {
            return webhook;
        }

        public String getTenantName() {
            return tenantName;
        }
    }
}