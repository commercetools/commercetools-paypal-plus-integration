package com.commercetools.payment.notification;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static com.commercetools.payment.constants.Psp.PSP_NAME;
import static java.lang.String.format;

@RestController
public class CommercetoolsPaymentNotificationController {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/"+ PSP_NAME + "/notification")
    public String handlePayments(@PathVariable String tenantName,
                                 HttpServletRequest request) {
        return format("Requested tenant [%s] with data: [%s]", tenantName,
                new HashMap<>(request.getParameterMap()).toString());
    }
}
