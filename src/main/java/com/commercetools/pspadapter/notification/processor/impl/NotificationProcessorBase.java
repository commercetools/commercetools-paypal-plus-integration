package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.exception.NotFoundException;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.lang.String.format;

public abstract class NotificationProcessorBase implements NotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessorBase.class);

    private final Gson gson;

    NotificationProcessorBase(Gson gson) {
        this.gson = gson;
    }

    abstract Optional<ChangeTransactionState> createChangeTransactionState(@Nonnull Payment ctpPayment);

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

    protected ImmutableList<UpdateAction<Payment>> createPaymentUpdates(@Nonnull Payment ctpPayment,
                                                                        @Nonnull Event event) {
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

    @SuppressWarnings("unchecked")
    protected CompletionStage<Optional<Payment>> getRelatedCtpPayment(@Nonnull CtpFacade ctpFacade,
                                                                      @Nonnull Event event) {
        Map<String, String> resource = (Map<String, String>) event.getResource();
        String ppPlusPaymentId = resource.get("parent_payment");

        return ctpFacade.getPaymentService()
                .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, ppPlusPaymentId);
    }

    protected AddInterfaceInteraction createAddInterfaceInteractionAction(@Nonnull PayPalModel model) {
        String json = gson.toJson(model);
        return AddInterfaceInteraction.ofTypeKeyAndObjects(InterfaceInteractionType.NOTIFICATION.getInterfaceKey(),
                ImmutableMap.of(InterfaceInteractionType.NOTIFICATION.getValueFieldName(), json,
                        "timestamp", ZonedDateTime.now()));
    }

}