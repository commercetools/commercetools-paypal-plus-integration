package com.commercetools.payment.handler.commandObject;

import org.hibernate.validator.constraints.NotBlank;

public class PaypalPlusExecuteParams {

    @NotBlank
    private String paypalPlusPaymentId;

    @NotBlank
    private String paypalPlusPayerId;

    public String getPaypalPlusPaymentId() {
        return paypalPlusPaymentId;
    }

    public void setPaypalPlusPaymentId(String paypalPlusPaymentId) {
        this.paypalPlusPaymentId = paypalPlusPaymentId;
    }

    public String getPaypalPlusPayerId() {
        return paypalPlusPayerId;
    }

    public void setPaypalPlusPayerId(String paypalPlusPayerId) {
        this.paypalPlusPayerId = paypalPlusPayerId;
    }
}