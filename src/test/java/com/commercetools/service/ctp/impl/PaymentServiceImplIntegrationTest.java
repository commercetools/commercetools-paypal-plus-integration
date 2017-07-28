package com.commercetools.service.ctp.impl;

import com.commercetools.Application;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.*;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.payments.commands.updateactions.ChangeAmountPlanned;
import io.sphere.sdk.payments.commands.updateactions.SetKey;
import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static org.assertj.core.api.Assertions.assertThat;

// TODO: move to separate integration test

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
// completely wipe-out CTP project Payment, Cart, Order endpoints before the test cases
public class PaymentServiceImplIntegrationTest {


    @Autowired
    private SphereClient sphereClient;

    @Autowired
    private PaymentService paymentService;

    @Test
    public void createManuallyAndGetById() throws Exception {

        PaymentDraftDsl eur = PaymentDraftBuilder.of(Money.of(22.33, EUR))
                .key("blah-blah")
                .build();

        Payment paymentCreated = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(eur)));
        Payment paymentRead = executeBlocking(paymentService.getById(paymentCreated.getId())).orElse(null);

        assertThat(paymentCreated).isEqualTo(paymentRead);
        assertThat(paymentRead.getAmountPlanned()).isEqualTo(Money.of(22.33, EUR));
        assertThat(paymentRead.getKey()).isEqualTo("blah-blah");
    }

    @Test
    public void createManuallyAndGetWithWrongId() throws Exception {
        PaymentDraftDsl eur = PaymentDraftBuilder.of(Money.of(22.33, EUR))
                .build();

        executeBlocking(sphereClient.execute(PaymentCreateCommand.of(eur)));
        assertThat(executeBlocking(paymentService.getById(null))).isEmpty();
        assertThat(executeBlocking(paymentService.getById(""))).isEmpty();
        assertThat(executeBlocking(paymentService.getById(" "))).isEmpty();
        assertThat(executeBlocking(paymentService.getById("asdfadsfasdf"))).isEmpty();
    }

    @Test
    public void shouldGetPaymentByPaymentMethodAndInterfaceId() {
        String paymentInterface = "testPaymentInterface";
        PaymentMethodInfo testMethodInfo = PaymentMethodInfoBuilder.of()
                .paymentInterface(paymentInterface)
                .build();
        String interfaceId = "testInterfaceId";
        PaymentDraftDsl draft = PaymentDraftBuilder.of(Money.of(22.33, EUR))
                .paymentMethodInfo(testMethodInfo)
                .interfaceId(interfaceId)
                .build();

        executeBlocking(sphereClient.execute(PaymentCreateCommand.of(draft)));
        Optional<Payment> payment = executeBlocking(
                paymentService.getByPaymentMethodAndInterfaceId(paymentInterface, interfaceId)
        );
        assertThat(payment).isNotEmpty();
    }

    @Test
    public void shouldUpdateAmountPlanned() {
        String paymentKey = "testPayment1";
        Money amountBefore = Money.of(1, EUR);
        PaymentDraftDsl draft = PaymentDraftBuilder.of(amountBefore)
                .key(paymentKey)
                .build();
        Payment payment = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(draft)));

        Money amountAfter = Money.of(2, EUR);
        List<UpdateAction<Payment>> updateActions = Collections.singletonList(ChangeAmountPlanned.of(amountAfter));
        Payment updatedPayment = executeBlocking(
                paymentService.updatePayment(payment, updateActions)
                        .thenCompose(p -> paymentService.getById(p.getId())))
                .orElse(null);
        assertThat(updatedPayment).isNotEqualTo(payment);
        assertThat(updatedPayment.getKey()).isEqualTo(paymentKey);
        assertThat(updatedPayment.getAmountPlanned()).isEqualTo(amountAfter);
    }

    @Test
    public void shouldUpdateMultipleAttributes() {
        Money amountBefore = Money.of(1, EUR);
        PaymentDraftDsl draft = PaymentDraftBuilder.of(amountBefore)
                .build();
        Payment payment = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(draft)));

        Money amountAfter = Money.of(2, EUR);
        String paymentKey = "testPayment2";
        List<UpdateAction<Payment>> updateActions = Arrays.asList(
                ChangeAmountPlanned.of(amountAfter),
                SetKey.of(paymentKey)
        );
        Payment updatedPayment = executeBlocking(
                paymentService.updatePayment(payment, updateActions)
                        .thenCompose(p -> paymentService.getById(p.getId())))
                .orElse(null);
        assertThat(updatedPayment).isNotEqualTo(payment);
        assertThat(updatedPayment.getKey()).isEqualTo(paymentKey);
        assertThat(updatedPayment.getAmountPlanned()).isEqualTo(amountAfter);
    }

    @Test
    public void whenUpdateActionsIsNullOrEmpty_shouldNotUpdateAnything() {
        PaymentDraftDsl draft = PaymentDraftBuilder.of(Money.of(1, EUR))
                .build();
        Payment payment = executeBlocking(sphereClient.execute(PaymentCreateCommand.of(draft)));

        Payment paymentAfterUpdate = executeBlocking(paymentService.updatePayment(payment, Collections.emptyList()));
        assertThat(paymentAfterUpdate).isEqualTo(payment);

        paymentAfterUpdate = executeBlocking(paymentService.updatePayment(payment, null));
        assertThat(paymentAfterUpdate).isEqualTo(payment);
    }
}