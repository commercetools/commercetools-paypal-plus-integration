package com.commercetools.payment;

public class Payment {

    private final String tenantName;
    private final String paymentId;

    public Payment(String tenantName, String paymentId) {
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
