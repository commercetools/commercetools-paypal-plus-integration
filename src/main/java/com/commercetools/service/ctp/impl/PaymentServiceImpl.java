package com.commercetools.service.ctp.impl;

import com.commercetools.exception.CtpServiceException;
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

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PaymentServiceImpl extends BaseSphereService implements PaymentService {

    public PaymentServiceImpl(@Nonnull SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Optional<Payment>> getById(@Nullable String id) {
        return isBlank(id)
                ? completedFuture(empty())
                : sphereClient.execute(PaymentByIdGet.of(id))
                .thenApply(Optional::ofNullable);
    }

    @Override
    public CompletionStage<Optional<Payment>> getByPaymentInterfaceNameAndInterfaceId(@Nullable String paymentInterfaceName, @Nullable String interfaceId) {
        if (isBlank(paymentInterfaceName) || isBlank(interfaceId)) {
            return completedFuture(empty());
        }
        PaymentQuery paymentQuery = PaymentQuery.of()
                .withPredicates(p -> p.paymentMethodInfo().paymentInterface().is(paymentInterfaceName))
                .plusPredicates(p -> p.interfaceId().is(interfaceId));
        return sphereClient.execute(paymentQuery)
                .thenApply(PagedResult::head);
    }

    @Override
    public CompletionStage<Payment> updatePayment(@Nonnull Payment payment, @Nullable List<UpdateAction<Payment>> updateActions) {
        return returnSameInstanceIfEmptyListOrExecuteCommand(payment, updateActions, PaymentUpdateCommand::of);
    }

    @Override
    public CompletionStage<Payment> updatePayment(@Nonnull String paymentId, @Nullable List<UpdateAction<Payment>> updateActions) {
        return getById(paymentId)
                .thenApply(paymentOpt -> paymentOpt.orElseThrow(() ->
                        new CtpServiceException(format("Update payment error: payment [%s] has not found", paymentId))))
                .thenCompose(payment -> updatePayment(payment, updateActions));
    }
}
