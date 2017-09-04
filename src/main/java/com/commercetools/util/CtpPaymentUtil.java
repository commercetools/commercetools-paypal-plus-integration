package com.commercetools.util;

import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

public final class CtpPaymentUtil {

    public static Optional<Transaction> findTransactionByTypeAndState(@Nonnull Collection<Transaction> transactions,
                                                                      @Nonnull TransactionType transactionType,
                                                                      @Nonnull TransactionState transactionState) {
        return transactions
                .stream()
                .filter(transaction -> transaction.getType().equals(transactionType))
                .filter(transaction -> transaction.getState().equals(transactionState))
                .findAny();
    }

    private CtpPaymentUtil() {
    }
}