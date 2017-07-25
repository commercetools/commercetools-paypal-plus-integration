package com.commercetools.pspadapter.facade;

import com.commercetools.service.ctp.CartService;
import com.commercetools.service.ctp.OrderService;
import com.commercetools.service.ctp.PaymentService;

import javax.annotation.Nonnull;

/**
 * A wrapper class for all services that communicate with CT platform
 */
public class CtpFacade {
    private final PaymentService paymentService;
    private final CartService cartService;
    private final OrderService orderService;

    public CtpFacade(@Nonnull CartService cartService,
                     @Nonnull OrderService orderService,
                     @Nonnull PaymentService paymentService) {
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