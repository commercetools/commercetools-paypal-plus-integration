package com.commercetools.service.main;

import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;
import com.commercetools.service.ctp.impl.CartServiceImpl;
import com.commercetools.service.ctp.impl.OrderServiceImpl;
import com.commercetools.service.ctp.impl.PaymentServiceImpl;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.commercetools.service.paypalPlus.impl.PaypalPlusPaymentServiceImpl;
import com.paypal.base.rest.APIContext;
import io.sphere.sdk.client.SphereClient;

public class PaymentHandler {

    private CartService cartService;

    private OrderService orderService;

    private PaymentService paymentService;

    private PaypalPlusPaymentService paypalPlusPaymentService;

    public PaymentHandler(SphereClient sphereClient, APIContext apiContext) {
        this.cartService = new CartServiceImpl(sphereClient);
        this.orderService = new OrderServiceImpl(sphereClient);
        this.paymentService = new PaymentServiceImpl(sphereClient);
        this.paypalPlusPaymentService = new PaypalPlusPaymentServiceImpl(apiContext);
    }

    public void handlePayment(){
        // implement method
    }
}