package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.exception.NotFoundException;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import com.paypal.base.rest.PayPalModel;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.lang.String.format;

public abstract class NotificationProcessorBase implements NotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessorBase.class);

    private final Gson gson;

    NotificationProcessorBase(Gson gson) {
        this.gson = gson;
    }

    @Override
    public CompletionStage<Payment> processEventNotification(@Nonnull CtpFacade ctpFacade,
                                                             @Nonnull Event event) {
        if (!canProcess(event)) {
            throw new IllegalArgumentException();
        }
        return getRelatedCtpPayment(ctpFacade, event)
                .thenCompose(ctpPaymentOpt -> ctpPaymentOpt.map(ctpPayment -> ctpFacade.getPaymentService()
                        .updatePayment(ctpPayment, createPaymentUpdates(ctpPayment, event)))
                        .orElseThrow(() -> new NotFoundException(format("No related CTP payment found for event [%s]", event.toJSON())))
                );
    }

    protected ImmutableList<UpdateAction<Payment>> createPaymentUpdates(Payment ctpPayment, Event event) {
        final ImmutableList.Builder<UpdateAction<Payment>> listBuilder = ImmutableList.builder();
        listBuilder.add(createAddInterfaceInteractionAction(event));
        Optional<ChangeTransactionState> changeTransactionOpt = createChangeTransactionState(ctpPayment);
        if (changeTransactionOpt.isPresent()) {
            listBuilder.add(changeTransactionOpt.get());
        } else {
            logger.warn("Notification event {} did not trigger change transaction state", event);
        }
        return listBuilder.build();
    }

    protected Optional<Transaction> findMatchingTxn(Collection<Transaction> transactions,
                                                    TransactionType transactionType,
                                                    TransactionState transactionState) {
        return transactions
                .stream()
                .filter(transaction -> transaction.getType().equals(transactionType))
                .filter(transaction -> transaction.getState().equals(transactionState))
                .findAny();
    }

    abstract Optional<ChangeTransactionState> createChangeTransactionState(Payment ctpPayment);

    abstract CompletionStage<Optional<Payment>> getRelatedCtpPayment(CtpFacade ctpFacade, Event event);

    private AddInterfaceInteraction createAddInterfaceInteractionAction(PayPalModel model) {
        String json = gson.toJson(model);
        return AddInterfaceInteraction.ofTypeKeyAndObjects(InterfaceInteractionType.NOTIFICATION.getInterfaceKey(),
                ImmutableMap.of(InterfaceInteractionType.NOTIFICATION.getValueFieldName(), json,
                        "timestamp", ZonedDateTime.now()));
    }

}