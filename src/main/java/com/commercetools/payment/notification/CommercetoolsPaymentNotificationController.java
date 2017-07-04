package com.commercetools.payment.notification;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.commercetools.payment.constants.Psp.PSP_NAME;
import static java.lang.String.format;

@RestController
public class CommercetoolsPaymentNotificationController {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/{tenantName}/"+ PSP_NAME + "/notification")
    public String handlePayments(@PathVariable String tenantName,
                                 HttpServletRequest request) {


        // skeleton for tests: just "reflect" the tenant name and request arguments
        String requestParameters = request.getParameterMap().entrySet().stream()
                .map(stringEntry -> format("%s=%s", stringEntry.getKey(), Arrays.stream(stringEntry.getValue())
                        .findFirst().orElse("")))
                .collect(Collectors.joining("\n"));

        return format("Requested tenant [%s] with data:%n%s%n", tenantName,
                requestParameters);
    }
}
