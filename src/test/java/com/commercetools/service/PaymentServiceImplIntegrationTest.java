package com.commercetools.service;

import com.commercetools.Application;
import com.commercetools.testUtil.customTestConfigs.PaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static org.assertj.core.api.Assertions.assertThat;

// TODO: move to separate integration test

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Import(PaymentsCleanupConfiguration.class) // completely wipe-out CTP project Payment endpoint before the test cases
public class PaymentServiceImplIntegrationTest {


    @Autowired
    private SphereClient sphereClient;

    @Autowired
    private PaymentService paymentService;

    @Test
    public void createManuallyAndGetById() throws Exception {

        PaymentDraftDsl eur = PaymentDraftBuilder.of(Money.of(22.33, "EUR"))
                .key("blah-blah")
                .build();

        Payment paymentCreated = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(eur)));
        Payment paymentRead = executeBlocking(paymentService.getById(paymentCreated.getId()));

        assertThat(paymentCreated).isEqualTo(paymentRead);
        assertThat(paymentRead.getAmountPlanned()).isEqualTo(Money.of(22.33, "EUR"));
        assertThat(paymentRead.getKey()).isEqualTo("blah-blah");
    }

    @Test
    public void createManuallyAndGetWithWrongId() throws Exception {
        PaymentDraftDsl eur = PaymentDraftBuilder.of(Money.of(22.33, "EUR"))
                .build();

        executeBlocking(sphereClient.execute(PaymentCreateCommand.of(eur)));
        assertThat(executeBlocking(paymentService.getById(null))).isNull();
        assertThat(executeBlocking(paymentService.getById(""))).isNull();
        assertThat(executeBlocking(paymentService.getById(" "))).isNull();
        assertThat(executeBlocking(paymentService.getById("asdfadsfasdf"))).isNull();
    }
}