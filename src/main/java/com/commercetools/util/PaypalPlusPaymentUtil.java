package com.commercetools.util;

import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public final class PaypalPlusPaymentUtil {

    /**
     * Get first non-null transaction from the {@code payment}
     * @param payment Paypal Plus {@link Payment}
     * @return first transaction from the list, if available.
     */
    public static Optional<Transaction> getFirstTransactionFromPayment(@Nullable Payment payment) {
        return ofNullable(payment)
                .map(Payment::getTransactions)
                .map(List::stream)
                .flatMap(Stream::findFirst);
    }

    /**
     * Gets first transaction in the payment's list, fetch first found {@link PaypalPlusPaymentIntent#SALE} transaction
     * from it.
     *
     * @param payment payment to process
     * @return Optional of {@link Sale} if found in the payment's transaction list.
     */
    public static Optional<Sale> getFirstSaleTransactionFromPayment(@Nullable Payment payment) {
        return getFirstTransactionFromPayment(payment)
                .map(Transaction::getRelatedResources)
                .map(List::stream)
                .flatMap(relatedResourcesStream -> relatedResourcesStream
                        .map(RelatedResources::getSale)
                        .filter(Objects::nonNull)
                        .findFirst());
    }

    private PaypalPlusPaymentUtil() {
    }
}
