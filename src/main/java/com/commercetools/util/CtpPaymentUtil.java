package com.commercetools.util;

import io.sphere.sdk.payments.Transaction;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class CtpPaymentUtil {

    public static Optional<Transaction> findTransactionByInteractionId(@Nonnull Collection<Transaction> transactions,
                                                                       @Nonnull String interactionId) {
        return transactions
                .stream()
                .filter(transaction -> Objects.equals(transaction.getInteractionId(), interactionId))
                .findFirst();
    }

    private CtpPaymentUtil() {
    }
}