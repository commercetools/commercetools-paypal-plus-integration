package com.commercetools.pspadapter.notification.processor;

import com.commercetools.pspadapter.facade.CtpFacade;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletionStage;

/**
 * Processes Paypal Plus event notifications
 */
public interface NotificationProcessor {

    CompletionStage<Payment> processEventNotification(@Nonnull CtpFacade ctpFacade, @Nonnull Event event);

    boolean canProcess(@Nonnull Event event);
}