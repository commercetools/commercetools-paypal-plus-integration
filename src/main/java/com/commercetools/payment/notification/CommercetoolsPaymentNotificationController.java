package com.commercetools.payment.notification;

import com.commercetools.model.PaypalPlusNotificationEvent;
import com.commercetools.payment.handler.BaseCommercetoolsPaymentsController;
import com.commercetools.pspadapter.notification.NotificationEventDispatcherProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;

import static com.commercetools.payment.constants.Psp.PSP_NAME;
import static java.lang.String.format;

@RestController
public class CommercetoolsPaymentNotificationController extends BaseCommercetoolsPaymentsController {

    private final NotificationEventDispatcherProvider eventDispatcherProvider;

    @Autowired
    public CommercetoolsPaymentNotificationController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                                      @Nonnull NotificationEventDispatcherProvider eventDispatcherProvider) {
        super(stringTrimmerEditor);
        this.eventDispatcherProvider = eventDispatcherProvider;
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/" + PSP_NAME + "/notification")
    public String handlePayments(@PathVariable String tenantName,
                                 @RequestBody PaypalPlusNotificationEvent eventFromPaypal) {
        eventDispatcherProvider.getNotificationDispatcher(tenantName)
                .map(notificationDispatcher -> {
                    notificationDispatcher.dispatchEvent(eventFromPaypal);
                    return null;
                });
        return format("Requested tenant [%s] with data:%n%s%n", tenantName,
                eventFromPaypal.toString());
    }
}
