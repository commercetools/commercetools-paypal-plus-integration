package com.commercetools.service;

import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface PaymentService {

    CompletionStage<Payment> getById(@Nullable String id);

    /**
     * Fetch payment with by its {@link Payment#getInterfaceId()} and {@link PaymentMethodInfo#getPaymentInterface()}.
     *
     * @param paymentMethodInterface name of payment interface, like "PAYONE"
     * @param interfaceId the payment's {@link Payment#getInterfaceId()}
     * @return completion stage with optional found payment.
     */
    CompletionStage<Optional<Payment>> getByPaymentMethodAndInterfaceId(String paymentMethodInterface, String interfaceId);
}
