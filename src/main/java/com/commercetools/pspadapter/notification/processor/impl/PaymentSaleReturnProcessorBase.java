package com.commercetools.pspadapter.notification.processor.impl;

import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public abstract class PaymentSaleReturnProcessorBase extends PaymentSaleNotificationProcessorBase {

    private final static Logger logger = LoggerFactory.getLogger(PaymentSaleReturnProcessorBase.class);

    PaymentSaleReturnProcessorBase(Gson gson) {
        super(gson);
    }

    List<? extends UpdateAction<Payment>> createUpdateCtpTransactionActions(@Nonnull Payment ctpPayment,
                                                                            @Nonnull Event event,
                                                                            @Nonnull TransactionType transactionType) {
        try {
            return singletonList(createAddTransactionAction(event, transactionType));
        } catch (Throwable t) {
            logger.error("Error when create update actions for eventId={}, ctpPaymentId={}, transactionType={}",
                    event.getId(), ctpPayment.getId(), transactionType, t);
            return emptyList();
        }
    }
}
