package com.commercetools.pspadapter.notification.validation;

import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacadeFactory;
import com.commercetools.pspadapter.notification.webhook.WebhookContainer;
import com.commercetools.pspadapter.notification.webhook.impl.WebhookContainerImpl;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.google.common.collect.ImmutableMap;
import com.paypal.api.payments.Webhook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotificationValidationInterceptorTest {

    @Test
    public void whenRequestIsValid_shouldReturnTrue() throws Exception {
        HttpServletRequest spyRequest = new MockHttpServletRequest();
        spyRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                ImmutableMap.of("tenantName", MAIN_TEST_TENANT_NAME));

        TenantConfigFactory configFactory = mock(TenantConfigFactory.class);

        TenantConfig tenantConfig = mock(TenantConfig.class);
        when(configFactory.getTenantConfig(anyString())).thenReturn(Optional.of(tenantConfig));
        when(tenantConfig.createAPIContextFactory()).thenReturn(mock(APIContextFactory.class));

        PaypalPlusPaymentService paymentService = mock(PaypalPlusPaymentService.class);
        when(paymentService.validateNotificationEvent(any(), anyMapOf(String.class, String.class), any())).thenReturn(completedFuture(true));

        PaypalPlusFacade paypalPlusFacade = mock(PaypalPlusFacade.class);
        when(paypalPlusFacade.getPaymentService()).thenReturn(paymentService);

        WebhookContainer mockContainer = mock(WebhookContainerImpl.class);
        when(mockContainer.getWebhookCompletionStageByTenantName(anyString())).thenReturn(CompletableFuture.completedFuture(new Webhook()));

        NotificationValidationInterceptor theObject = new NotificationValidationInterceptor(mockContainer, configFactory, new PaypalPlusFacadeFactory());
        NotificationValidationInterceptor interceptor = spy(theObject);
        doReturn(paypalPlusFacade).when(interceptor).getPaypalPlusFacade(any());

        boolean result = interceptor.preHandle(spyRequest, new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
    }
}