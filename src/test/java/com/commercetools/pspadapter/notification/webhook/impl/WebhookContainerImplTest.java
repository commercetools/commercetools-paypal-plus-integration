package com.commercetools.pspadapter.notification.webhook.impl;

import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.Webhook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebhookContainerImplTest {

    private static final String TEST_TENANT_NAME = "testTenant";

    @Test
    public void shouldReturnWebhook () {
        // setup
        TenantConfig mockTenantConfig = mock(TenantConfig.class);
        PaypalPlusFacadeFactory mockFactory = mock(PaypalPlusFacadeFactory.class);
        PaypalPlusFacade mockFacade =  mock(PaypalPlusFacade.class);
        PaypalPlusPaymentService mockService = mock(PaypalPlusPaymentService.class);
        Webhook mockWebhook = mock(Webhook.class);
        when(mockTenantConfig.getTenantName()).thenReturn(TEST_TENANT_NAME);
        when(mockFactory.getPaypalPlusFacade(anyObject())).thenReturn(mockFacade);
        when(mockFacade.getPaymentService()).thenReturn(mockService);
        when(mockService.ensureWebhook(anyString())).thenReturn(CompletableFuture.completedFuture(mockWebhook));

        // test
        WebhookContainerImpl webhookContainer = new WebhookContainerImpl(Collections.singletonList(mockTenantConfig), mockFactory, "test");
        Webhook webhook = webhookContainer.getWebhookCompletionStageByTenantName(TEST_TENANT_NAME).toCompletableFuture().join();

        // assert
        assertThat(webhook).isEqualTo(mockWebhook);
    }

    @Test
    public void shouldReturnNullIfNoWebhookExist() {
        // setup
        TenantConfig mockTenantConfig = mock(TenantConfig.class);
        PaypalPlusFacadeFactory mockFactory = mock(PaypalPlusFacadeFactory.class);
        PaypalPlusFacade mockFacade =  mock(PaypalPlusFacade.class);
        PaypalPlusPaymentService mockService = mock(PaypalPlusPaymentService.class);
        Webhook mockWebhook = mock(Webhook.class);
        when(mockTenantConfig.getTenantName()).thenReturn(TEST_TENANT_NAME);
        when(mockFactory.getPaypalPlusFacade(anyObject())).thenReturn(mockFacade);
        when(mockFacade.getPaymentService()).thenReturn(mockService);
        when(mockService.ensureWebhook(anyString())).thenReturn(CompletableFuture.completedFuture(mockWebhook));

        // test
        WebhookContainerImpl webhookContainer = new WebhookContainerImpl(Collections.singletonList(mockTenantConfig), mockFactory, "test");
        Webhook webhook = webhookContainer.getWebhookCompletionStageByTenantName("test").toCompletableFuture().join();

        // assert
        assertThat(webhook).isNull();
    }
}