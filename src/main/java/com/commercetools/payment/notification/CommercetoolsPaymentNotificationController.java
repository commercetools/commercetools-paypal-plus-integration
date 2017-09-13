package com.commercetools.payment.notification;

import com.commercetools.model.PaypalPlusNotificationEvent;
import com.commercetools.payment.handler.BaseCommercetoolsController;
import com.commercetools.pspadapter.notification.NotificationEventDispatcherProvider;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;
import com.commercetools.web.bind.annotation.PostJsonRequestJsonResponseMapping;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;
import static com.commercetools.util.IOUtil.getBody;
import static java.lang.String.format;

/**
 * Process the valid notification event from Paypal Plus. Validation is done in
 * {@link NotificationValidationInterceptor}.
 * <p>
 * For devs: validation of Paypal notification cannot be done here because it needs HTTP request headers and body. However,
 * {@link CommercetoolsPaymentNotificationController#handleNotification(String, HttpServletRequest)} also requires
 * RequestBody to parse the Event object. In Spring, you cannot inject both RequestBody and HttpRequest in one method
 * and then read the request body. It will throw an error.
 */
@RestController
public class CommercetoolsPaymentNotificationController extends BaseCommercetoolsController {

    private final NotificationEventDispatcherProvider eventDispatcherProvider;

    private final Logger logger;

    private final Gson paypalGson;

    @Autowired
    public CommercetoolsPaymentNotificationController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                      @Nonnull NotificationEventDispatcherProvider eventDispatcherProvider,
                                                      @Nonnull Gson paypalGson) {
        super(stringTrimmerEditor);
        this.eventDispatcherProvider = eventDispatcherProvider;
        this.paypalGson = paypalGson;
        logger = LoggerFactory.getLogger(PaymentHandler.class);
    }

    @PostJsonRequestJsonResponseMapping(value = "/{tenantName}/" + NOTIFICATION_PATH_URL)
    public CompletionStage<ResponseEntity> handleNotification(@PathVariable String tenantName,
                                                              @Nonnull HttpServletRequest request) throws IOException {
        PaypalPlusNotificationEvent eventFromPaypal = paypalGson.fromJson(getBody(request), PaypalPlusNotificationEvent.class);
        return eventDispatcherProvider.getNotificationDispatcher(tenantName)
                .map(notificationDispatcher -> notificationDispatcher.handleEvent(eventFromPaypal, tenantName))
                .orElseGet(() -> {
                    // we don't return any error in this case, because notification is mostly send automatically
                    // and in case of error response, paypal can retry the notification again and again
                    logger.error(format("No notification handler found for tenant [%s] and event [%s].",
                            tenantName, eventFromPaypal.toJSON()));
                    return CompletableFuture.completedFuture(PaymentHandleResponse.ofHttpStatus(HttpStatus.OK));
                })
                .thenApply(PaymentHandleResponse::toResponseEntity);
    }
}
