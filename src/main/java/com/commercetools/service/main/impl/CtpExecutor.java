package com.commercetools.service.main.impl;

import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;

public class CtpExecutor {
    private final PaymentService paymentService;
    private final CartService cartService;
    private final OrderService orderService;

    public CtpExecutor(CartService cartService,
                       OrderService orderService,
                       PaymentService paymentService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public CartService getCartService() {
        return cartService;
    }

    public OrderService getOrderService() {
        return orderService;
    }
}