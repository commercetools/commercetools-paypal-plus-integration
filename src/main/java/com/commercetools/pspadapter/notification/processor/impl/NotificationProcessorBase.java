package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.exception.NotFoundException;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.notification.processor.NotificationProcessor;
import com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType;
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

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.lang.String.format;

public abstract class NotificationProcessorBase implements NotificationProcessor {

    private final Gson gson;

    NotificationProcessorBase(Gson gson) {
        this.gson = gson;
    }

    abstract List<? extends UpdateAction<Payment>>  createChangeTransactionState(@Nonnull Payment ctpPayment);

    @Override
    public CompletionStage<Payment> processEventNotification(@Nonnull CtpFacade ctpFacade,
                                                             @Nonnull Event event) {
        if (!canProcess(event)) {
            throw new IllegalArgumentException(format("Event can't be processed. [%s]", event.toJSON()));
        }
        return getRelatedCtpPayment(ctpFacade, event)
                .thenCompose(ctpPaymentOpt -> ctpPaymentOpt.map(ctpPayment -> ctpFacade.getPaymentService()
                        .updatePayment(ctpPayment, createPaymentUpdates(ctpPayment, event)))
                        .orElseThrow(() -> new NotFoundException(format("No related CTP payment found for event [%s]", event.toJSON())))
                );
    }

    protected List<UpdateAction<Payment>> createPaymentUpdates(@Nonnull Payment ctpPayment,
                                                                        @Nonnull Event event) {
        ArrayList<UpdateAction<Payment>> updateActions = new ArrayList<>();
        updateActions.add(createAddInterfaceInteractionAction(event));
        List<? extends UpdateAction<Payment>> updateAtions = createChangeTransactionState(ctpPayment);
        if (!updateAtions.isEmpty()) {
            updateActions.addAll(updateAtions);
        }
        return updateActions;
    }

    @SuppressWarnings("unchecked")
    protected CompletionStage<Optional<Payment>> getRelatedCtpPayment(@Nonnull CtpFacade ctpFacade,
                                                                      @Nonnull Event event) {
        Map<String, String> resource = (Map<String, String>) event.getResource();
        String ppPlusPaymentId = resource.get("parent_payment");

        return ctpFacade.getPaymentService()
                .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, ppPlusPaymentId);
    }

    protected Optional<Transaction> findMatchingTxn(@Nonnull Collection<Transaction> transactions,
                                                    @Nonnull TransactionType transactionType,
                                                    @Nonnull TransactionState transactionState) {
        return transactions
                .stream()
                .filter(transaction -> transaction.getType().equals(transactionType))
                .filter(transaction -> transaction.getState().equals(transactionState))
                .findAny();
    }

    protected AddInterfaceInteraction createAddInterfaceInteractionAction(@Nonnull PayPalModel model) {
        String json = gson.toJson(model);
        return AddInterfaceInteraction.ofTypeKeyAndObjects(InterfaceInteractionType.NOTIFICATION.getInterfaceKey(),
                ImmutableMap.of(InterfaceInteractionType.NOTIFICATION.getValueFieldName(), json,
                        "timestamp", ZonedDateTime.now()));
    }

}