package com.commercetools.payment.notification;

import com.commercetools.model.PaypalPlusNotificationEvent;
import com.commercetools.payment.handler.BaseCommercetoolsController;
import com.commercetools.pspadapter.notification.NotificationEventDispatcherProvider;
import com.commercetools.pspadapter.notification.validation.NotificationValidationInterceptor;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;
import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Process the valid notification event from Paypal Plus. Validation is done in
 * {@link NotificationValidationInterceptor}.
 *
 * For devs: validation of Paypal notification cannot be done here because it needs HTTP request headers and body. However,
 * {@link CommercetoolsPaymentNotificationController#handleNotification(String, PaypalPlusNotificationEvent)} also requires
 * RequestBody to parse the Event object. In Spring, you cannot inject both RequestBody and HttpRequest in one method
 * and then read the request body. It will throw an error.
 */
@RestController
public class CommercetoolsPaymentNotificationController extends BaseCommercetoolsController {

    private final NotificationEventDispatcherProvider eventDispatcherProvider;

    private final Logger logger;

    @Autowired
    public CommercetoolsPaymentNotificationController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                      @Nonnull NotificationEventDispatcherProvider eventDispatcherProvider) {
        super(stringTrimmerEditor);
        this.eventDispatcherProvider = eventDispatcherProvider;
        logger = LoggerFactory.getLogger(PaymentHandler.class);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            consumes = APPLICATION_JSON_VALUE,
            value = "/{tenantName}/" + NOTIFICATION_PATH_URL)
    public ResponseEntity handleNotification(@PathVariable String tenantName,
                                                    @RequestBody PaypalPlusNotificationEvent eventFromPaypal) {
        return eventDispatcherProvider.getNotificationDispatcher(tenantName)
                .map(notificationDispatcher -> notificationDispatcher.handleEvent(eventFromPaypal, tenantName))
                .orElseGet(() -> {
                    // we don't return any error in this case, because notification is mostly send automatically
                    // and in case of error response, paypal can retry the notification again and again
                    logger.error(format("No notification handler found for tenant [%s] and event [%s].",
                            tenantName, eventFromPaypal.toJSON()));
                    return CompletableFuture.completedFuture(PaymentHandleResponse.ofHttpStatus(HttpStatus.OK));
                })
                .thenApply(PaymentHandleResponse::toResponseEntity)
                .toCompletableFuture().join();
    }

}
