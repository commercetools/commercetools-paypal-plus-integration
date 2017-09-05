package com.commercetools.model;

import com.paypal.api.payments.Event;

/**
 * Helper class for Spring to parse to request body to notification object
 */
public class PaypalPlusNotificationEvent extends Event {

    public void setEvent_type(String event_type) {
        this.setEventType(event_type);
    }

    public void setCreate_time(String create_time) {
        this.setCreateTime(create_time);
    }

    public void setResource_type(String resource_type) {
        this.setResourceType(resource_type);
    }
}