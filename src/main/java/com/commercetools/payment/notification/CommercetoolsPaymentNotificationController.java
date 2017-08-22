package com.commercetools.payment.notification;

import com.commercetools.model.PaypalPlusNotificationEvent;
import com.commercetools.payment.handler.BaseCommercetoolsPaymentsController;
import com.commercetools.pspadapter.notification.NotificationEventDispatcherProvider;
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

import static com.commercetools.payment.constants.Psp.PSP_NAME;
import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CommercetoolsPaymentNotificationController extends BaseCommercetoolsPaymentsController {

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
            value = "/{tenantName}/" + PSP_NAME + "/notification")
    public ResponseEntity<HttpStatus> handleNotification(@PathVariable String tenantName,
                                                     @RequestBody PaypalPlusNotificationEvent eventFromPaypal) {
        return eventDispatcherProvider.getNotificationDispatcher(tenantName)
                .map(notificationDispatcher -> notificationDispatcher.dispatchEvent(eventFromPaypal)
                        .handle((payment, throwable) -> {
                            if (throwable != null) {
                                logger.error(format("Unexpected exception processing event=[%s] for tenant=[%s]",
                                        eventFromPaypal.toJSON(), tenantName));
                                return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
                            } else {
                                return new ResponseEntity<HttpStatus>(HttpStatus.OK);
                            }
                        })
                        .toCompletableFuture()
                        .join())
                // we don't return any error in this case, because it doesn't make sense for them to repeat
                // the request if we don't support that type of notification
                .orElseGet(() -> {
                    logger.error(format("No notification handler found for tenant [%s] and event [%s].",
                            tenantName, eventFromPaypal.toJSON()));
                    return new ResponseEntity<>(HttpStatus.OK);
                });
    }
}
