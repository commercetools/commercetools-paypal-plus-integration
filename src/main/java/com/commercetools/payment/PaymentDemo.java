package com.commercetools.payment;

public class PaymentDemo {

    private final String tenantName;
    private final String paymentId;

    public PaymentDemo(String tenantName, String paymentId) {
        this.tenantName = tenantName;
        this.paymentId = paymentId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
