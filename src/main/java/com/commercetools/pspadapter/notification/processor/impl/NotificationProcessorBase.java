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
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.TIMESTAMP_FIELD;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.completedFuture;

public abstract class NotificationProcessorBase implements NotificationProcessor {

    private static final String PARENT_PAYMENT_ATTRIBUTE = "parent_payment";

    private final Gson gson;

    private final static Logger logger = LoggerFactory.getLogger(NotificationProcessorBase.class);

    NotificationProcessorBase(Gson gson) {
        this.gson = gson;
    }

    abstract List<? extends UpdateAction<Payment>> updateCtpTransactions(@Nonnull Payment ctpPayment, @Nonnull Event event);

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
        updateActions.addAll(updateCtpTransactions(ctpPayment, event));
        return updateActions;
    }

    protected CompletionStage<Optional<Payment>> getRelatedCtpPayment(@Nonnull CtpFacade ctpFacade,
                                                                      @Nonnull Event event) {
        try {
            Map resource = (Map) event.getResource();
            String ppPlusPaymentId = (String) resource.get(PARENT_PAYMENT_ATTRIBUTE);

            return ctpFacade.getPaymentService()
                    .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, ppPlusPaymentId);
        } catch (Throwable t) {
            logger.error("Error when getting related ctp payment for eventId={}", event.getId(), t);
            return completedFuture(empty());
        }
    }

    protected AddInterfaceInteraction createAddInterfaceInteractionAction(@Nonnull PayPalModel model) {
        String json = gson.toJson(model);
        return AddInterfaceInteraction.ofTypeKeyAndObjects(InterfaceInteractionType.NOTIFICATION.getInterfaceKey(),
                ImmutableMap.of(InterfaceInteractionType.NOTIFICATION.getValueFieldName(), json,
                        TIMESTAMP_FIELD, ZonedDateTime.now()));
    }

}