package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PaymentHandlerProviderImplTest {

    @Autowired
    private PaymentHandlerProviderImpl paymentHandlerProvider;

    @Test
    public void shouldReturnCorrectHandlerByTenantName() {
        Optional<PaymentHandler> paymentClientOpt = paymentHandlerProvider.getPaymentHandler(MAIN_TEST_TENANT_NAME);
        assertThat(paymentClientOpt).isNotEmpty();
    }

    @Test
    public void whenTenantDoesNotExist_shouldReturnEmptyOptional() {
        Optional<PaymentHandler> paymentClientOpt = paymentHandlerProvider.getPaymentHandler("");
        assertThat(paymentClientOpt).isEmpty();
    }
}