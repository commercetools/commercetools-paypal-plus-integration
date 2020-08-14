package com.commercetools.service.ctp.impl;

import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.commands.UpdateCommand;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.commands.PaymentUpdateCommand;
import io.sphere.sdk.payments.commands.updateactions.SetInterfaceId;
import io.sphere.sdk.payments.commands.updateactions.SetStatusInterfaceCode;
import io.sphere.sdk.payments.commands.updateactions.SetStatusInterfaceText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;

import static com.commercetools.testUtil.AssertUtil.assertThatUpdateActionList;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BaseSphereServiceTest {

    @Mock
    private SphereClient sphereClient;

    @Mock
    private BaseSphereService baseSphereService;

    @Mock
    private Payment payment;

    @Mock
    private Payment paymentUpdated;

    @Before
    public void setUp() throws Exception {
        baseSphereService = new TestableBaseSphereService(sphereClient);
    }

    @Test
    public void whenEmptyList_returnSameInstanceIfEmptyListOrExecuteCommand_returnsSameInstance() throws Exception {
        BiFunction<Payment, List<? extends UpdateAction<Payment>>, UpdateCommand<Payment>> throwIfCalled = (a, b) -> {
            throw new IllegalStateException("command supplier is not expected to be called when list is empty");
        };

        Payment newPayment = executeBlocking(baseSphereService.returnSameInstanceIfEmptyListOrExecuteCommand(this.payment, emptyList(), throwIfCalled));
        assertThat(newPayment).isSameAs(payment);

        newPayment = executeBlocking(baseSphereService.returnSameInstanceIfEmptyListOrExecuteCommand(this.payment, null, throwIfCalled));
        assertThat(newPayment).isSameAs(payment);

        // ensure sphere client is not called at all
        verify(sphereClient, never()).execute(anyObject());
    }

    @Test
    public void whenListHasValuesList_returnSameInstanceIfEmptyListOrExecuteCommand_callsSphereClient() throws Exception {
        when(sphereClient.execute(any(PaymentUpdateCommand.class))).thenReturn(completedFuture(paymentUpdated));

        // single action
        Payment newPayment = executeBlocking(baseSphereService.returnSameInstanceIfEmptyListOrExecuteCommand(this.payment,
                singletonList(SetInterfaceId.of("42")),
                PaymentUpdateCommand::of));
        assertThat(newPayment).isSameAs(paymentUpdated);

        ArgumentCaptor<PaymentUpdateCommand> commandCaptor = ArgumentCaptor.forClass(PaymentUpdateCommand.class);
        verify(sphereClient).execute(commandCaptor.capture());

        assertThatUpdateActionList(commandCaptor.getValue().getUpdateActions())
                .containsExactly(SetInterfaceId.of("42"));

        // multiple actions
        newPayment = executeBlocking(baseSphereService.returnSameInstanceIfEmptyListOrExecuteCommand(this.payment,
                asList(
                        SetInterfaceId.of("42"),
                        SetStatusInterfaceText.of("success"),
                        SetStatusInterfaceCode.of("666")),
                PaymentUpdateCommand::of));
        assertThat(newPayment).isSameAs(paymentUpdated);

        commandCaptor = ArgumentCaptor.forClass(PaymentUpdateCommand.class);
        // capture second call, so expected sphere client call twice and take values.get(1)
        verify(sphereClient, times(2)).execute(commandCaptor.capture());

        assertThatUpdateActionList(commandCaptor.getAllValues().get(1).getUpdateActions())
                .containsExactly(
                        SetInterfaceId.of("42"),
                        SetStatusInterfaceText.of("success"),
                        SetStatusInterfaceCode.of("666"));
    }

    private static class TestableBaseSphereService extends BaseSphereService {
        TestableBaseSphereService(@Nonnull SphereClient sphereClient) {
            super(sphereClient);
        }
    }

}