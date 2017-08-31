package com.commercetools.pspadapter.notification.processor.impl;

import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 *  Notification that return true for all events. This event is then saved in
 *  the current {@link Payment} as a Interface interaction
 */
@Component
public class DefaultNotificationProcessor extends NotificationProcessorBase {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessorBase.class);

    @Autowired
    DefaultNotificationProcessor(Gson gson) {
        super(gson);
    }

    @Override
    Optional<ChangeTransactionState> createChangeTransactionState(Payment ctpPayment) {
        return Optional.empty();
    }

    @Override
    public boolean canProcess(Event event) {
        logger.info("Cannot find processor for the event, using the default processor: {}", event);
        return true;
    }
}
