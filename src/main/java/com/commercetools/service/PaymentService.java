package com.commercetools.service;

import io.sphere.sdk.payments.Payment;

import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

public interface PaymentService {

    CompletionStage<Payment> getById(@Nullable String id);
}
