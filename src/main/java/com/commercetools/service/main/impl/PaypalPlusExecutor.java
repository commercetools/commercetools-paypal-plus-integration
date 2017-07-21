package com.commercetools.service.main.impl;

import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: add class description
 */
public class PaypalPlusExecutor {

    public PaypalPlusPaymentService paymentService;

    public PaypalPlusExecutor(PaypalPlusPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public PaypalPlusPaymentService getPaymentService() {
        return paymentService;
    }
}