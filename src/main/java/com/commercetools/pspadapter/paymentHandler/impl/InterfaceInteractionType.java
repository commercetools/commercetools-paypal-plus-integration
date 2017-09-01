package com.commercetools.pspadapter.paymentHandler.impl;

public enum InterfaceInteractionType {
    REQUEST("request", "paypal-plus-interaction-request"),
    RESPONSE("response", "paypal-plus-interaction-response");

    private final String valueFieldName;

    private final String interfaceKey;

    InterfaceInteractionType(String valueFieldName,
                             String interfaceKey) {
        this.interfaceKey = interfaceKey;
        this.valueFieldName = valueFieldName;
    }

    public String getInterfaceKey() {
        return interfaceKey;
    }

    public String getValueFieldName() {
        return valueFieldName;
    }
}
