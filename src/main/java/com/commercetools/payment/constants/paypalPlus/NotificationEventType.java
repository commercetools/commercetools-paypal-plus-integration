package com.commercetools.payment.constants.paypalPlus;

public enum NotificationEventType {
    PAYMENT_SALE_COMPLETED("PAYMENT.SALE.COMPLETED"),
    PAYMENT_SALE_DENIED("PAYMENT.SALE.DENIED"),
    PAYMENT_SALE_PENDING("PAYMENT.SALE.PENDING"),
    PAYMENT_SALE_REFUNDED("PAYMENT.SALE.REFUNDED"),
    PAYMENT_SALE_REVERSED("PAYMENT.SALE.REVERSED");

    private final String paypalEventTypeName;

    NotificationEventType(String paypalEventTypeName) {
        this.paypalEventTypeName = paypalEventTypeName;
    }

    public String getPaypalEventTypeName() {
        return paypalEventTypeName;
    }

    @Override
    public String toString() {
        return paypalEventTypeName;
    }
}