package com.commercetools.service.ctp.impl;

import com.commercetools.service.ctp.PaymentService;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.commands.PaymentUpdateCommand;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import io.sphere.sdk.payments.queries.PaymentQuery;
import io.sphere.sdk.queries.PagedResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PaymentServiceImpl extends BaseSphereService implements PaymentService {

    public PaymentServiceImpl(SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Payment> getById(@Nullable String id) {
        return isBlank(id) ? completedFuture(null) : sphereClient.execute(PaymentByIdGet.of(id));
    }

    @Override
    public CompletionStage<Optional<Payment>> getByPaymentMethodAndInterfaceId(@Nullable String paymentInterface, @Nullable String interfaceId) {
        if (isBlank(paymentInterface) || isBlank(interfaceId)) {
            return completedFuture(null);
        }
        PaymentQuery paymentQuery = PaymentQuery.of()
                .withPredicates(p -> p.paymentMethodInfo().paymentInterface().is(paymentInterface))
                .plusPredicates(p -> p.interfaceId().is(interfaceId));
        return sphereClient.execute(paymentQuery)
                .thenApply(PagedResult::head);
    }

    @Override
    public CompletionStage<Payment> updatePayment(@Nonnull Payment payment, @Nullable List<UpdateAction<Payment>> updateActions) {
        if (updateActions == null || updateActions.isEmpty()) {
            return completedFuture(payment);
        }
        return sphereClient.execute(PaymentUpdateCommand.of(payment, updateActions));
    }

}
