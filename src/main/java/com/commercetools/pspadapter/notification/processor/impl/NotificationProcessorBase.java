package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.exception.NotFoundException;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Event;
import com.paypal.base.rest.PayPalModel;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.lang.String.format;

public abstract class NotificationProcessorBase implements NotificationProcessor {


    @Override
    public CompletionStage<Payment> processEventNotification(CtpFacade ctpFacade, Event event) {
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
        listBuilder.add(createUpdatePaymentStatus(ctpPayment, event));
        return listBuilder.build();
    }

    private AddInterfaceInteraction createAddInterfaceInteractionAction(PayPalModel model) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .disableHtmlEscaping()
                .create();
        String json = gson.toJson(model);
        return AddInterfaceInteraction.ofTypeKeyAndObjects(InterfaceInteractionType.NOTIFICATION.getInterfaceKey(),
                ImmutableMap.of(InterfaceInteractionType.NOTIFICATION.getValueFieldName(), json,
                        "timestamp", ZonedDateTime.now()));
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

    abstract ChangeTransactionState createUpdatePaymentStatus(Payment ctpPayment, Event event);

    abstract CompletionStage<Optional<Payment>> getRelatedCtpPayment(CtpFacade ctpFacade, Event event);

}