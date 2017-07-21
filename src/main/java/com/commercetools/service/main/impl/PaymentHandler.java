package com.commercetools.service.main.impl;

public class PaymentHandler {

    private final CtpExecutor ctpExecutor;
    private final PaypalPlusExecutor paypalPlusExecutor;

    public PaymentHandler(CtpExecutor ctpExecutor,
                          PaypalPlusExecutor paypalPlusExecutor) {
        this.ctpExecutor = ctpExecutor;
        this.paypalPlusExecutor = paypalPlusExecutor;
    }

    public void handlePayment(String paymentId){
        ctpExecutor.getCartService().getByPaymentId(paymentId);
        // mapping
        paypalPlusExecutor.getPaymentService().create(null);
    }
}