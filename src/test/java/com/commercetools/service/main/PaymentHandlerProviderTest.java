package com.commercetools.service.main;

import com.commercetools.Application;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandlerProviderImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PaymentHandlerProviderTest {

    @Autowired
    private PaymentHandlerProviderImpl paymentHandlerProvider;

    @Test
    public void shouldReturnCorrectHandlerByTenantName() {
        Optional<PaymentHandler> paymentClientOpt = paymentHandlerProvider.getPaymentHandler("paypalplus-integration-test");
        assertThat(paymentClientOpt).isNotEmpty();
    }
}