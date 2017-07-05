package com.commercetools.service.impl;

import com.commercetools.service.PaymentService;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.queries.PaymentByIdGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class PaymentServiceImpl extends BaseSphereService implements PaymentService {

    @Autowired
    public PaymentServiceImpl(@Nonnull SphereClient sphereClient) {
        super(sphereClient);
    }

    @Override
    public CompletionStage<Payment> getById(@Nullable String id) {
        return isBlank(id) ? completedFuture(null) : sphereClient.execute(PaymentByIdGet.of(id));
    }
}
