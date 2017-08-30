package com.commercetools.pspadapter.paymentHandler.impl;

public enum InterfaceInteractionType {
    REQUEST("paypal-plus-interaction-request"),
    RESPONSE("paypal-plus-interaction-response"),
    NOTIFICATION("paypal-plus-interaction-notification");

    private final String interfaceKey;

    InterfaceInteractionType(String interfaceKey) {
        this.interfaceKey = interfaceKey;
    }

    public String getInterfaceKey() {
        return interfaceKey;
    }

    public String getValueFieldName() {
        return name().toLowerCase();
    }
}
