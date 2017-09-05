package com.commercetools.service.ctp;

import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface PaymentService {

    CompletionStage<Optional<Payment>> getById(@Nullable String id);

    /**
     * Fetch payment with by its {@link Payment#getInterfaceId()} and {@link PaymentMethodInfo#getPaymentInterface()}.
     *
     * @param paymentInterfaceName name of payment interface, like "PAYONE"
     * @param interfaceId            the payment's {@link Payment#getInterfaceId()}
     * @return completion stage with optional found payment.
     */
    CompletionStage<Optional<Payment>> getByPaymentInterfaceNameAndInterfaceId(@Nullable String paymentInterfaceName, @Nullable String interfaceId);

    /**
     * Apply {@code updateActions} to the {@code payment}
     *
     * @param payment       <b>non-null</b> {@link Payment} to update
     * @param updateActions <b>non-null</b> list of {@link UpdateAction <Payment>} to apply to the {@code payment}
     * @return Completion stage with an instance of the updated payment
     */
    CompletionStage<Payment> updatePayment(@Nonnull Payment payment, @Nullable List<UpdateAction<Payment>> updateActions);

    /**
     * Fetch and update payment by {@code paymentId}. If such payment does not exists -
     * throw {@link com.commercetools.exception.CtpServiceException}.
     *
     * @param paymentId     payment to update
     * @param updateActions list of actions to apply if such a payment exists
     * @return stage with new update payment
     */
    CompletionStage<Payment> updatePayment(@Nonnull String paymentId, @Nullable List<UpdateAction<Payment>> updateActions);
}
