package com.commercetools.helper.mapper.impl;

import com.commercetools.Application;
import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.CREDIT_CARD;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getPaymentWithCart_complexAndDiscount;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getPaymentWithCart_complexWithoutDiscount;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PaymentMapperImplTest {

    @Autowired
    private PaymentMapperImpl paymentMapper;

    @Test
    public void nullCase() throws Exception {
        assertThat(paymentMapper.ctpPaymentToPaypalPlus(null)).isNull();

    }

    @Test
    public void ctpPaymentToPaypalPlus_withDiscount() throws Exception {
        CtpPaymentWithCart paymentWithCart = getPaymentWithCart_complexAndDiscount();
        Payment ppPayment = paymentMapper.ctpPaymentToPaypalPlus(paymentWithCart);

        assertThat(ppPayment).isNotNull();

        assertThat(ppPayment.getCart()).isEqualTo("0756a2bb-f1c6-4a96-bcbb-7624f12b9c1a");

        assertThat(ppPayment.getIntent()).isEqualTo(SALE);

        assertThat(ppPayment.getPayer()).isNotNull();
        assertThat(ppPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);
        assertThat(ppPayment.getPayer().getFundingInstruments().get(0).getCreditCardToken().getCreditCardId()).isEmpty();

        assertThat(ppPayment.getState()).isNull();

        assertThat(ppPayment.getRedirectUrls().getReturnUrl()).isEqualTo("https://www.sparta.de/success/12333456");
        assertThat(ppPayment.getRedirectUrls().getCancelUrl()).isEqualTo("https://www.sparta.de/cancel/12333456");

        Transaction transaction = ppPayment.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("Payment from commercetools Paypal Plus integration service");

        Amount amount = transaction.getAmount();

        assertThat(amount.getCurrency()).isEqualTo("USD");
        assertThat(amount.getTotal()).isEqualTo("596.85");
    }

    @Test
    public void ctpPaymentToPaypalPlus_withoutDiscount() throws Exception {
        CtpPaymentWithCart paymentWithCart = getPaymentWithCart_complexWithoutDiscount();
        Payment ppPayment = paymentMapper.ctpPaymentToPaypalPlus(paymentWithCart);

        assertThat(ppPayment).isNotNull();

        assertThat(ppPayment.getCart()).isEqualTo("1236a456-7890-4a96-aaaa-abcd12b9ceee");

        assertThat(ppPayment.getIntent()).isEqualTo(SALE);

        assertThat(ppPayment.getPayer()).isNotNull();
        assertThat(ppPayment.getPayer().getPaymentMethod()).isEqualTo(CREDIT_CARD);
        assertThat(ppPayment.getPayer().getFundingInstruments().get(0).getCreditCardToken().getCreditCardId())
                .isEqualTo("CARD-2CP65563W6136533VLF3XGWQ");

        assertThat(ppPayment.getState()).isNull();

        assertThat(ppPayment.getRedirectUrls().getReturnUrl()).isEqualTo("https://www.sparta.de/success/556677884433");
        assertThat(ppPayment.getRedirectUrls().getCancelUrl()).isEqualTo("https://www.sparta.de/cancel/556677884433");

        Transaction transaction = ppPayment.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("Payment from commercetools Paypal Plus integration service");

        Amount amount = transaction.getAmount();
        assertThat(amount.getCurrency()).isEqualTo("EUR");
        assertThat(amount.getTotal()).isEqualTo("309.00");
    }

}