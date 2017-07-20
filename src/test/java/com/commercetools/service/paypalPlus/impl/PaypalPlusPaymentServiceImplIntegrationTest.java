package com.commercetools.service.paypalPlus.impl;

import com.commercetools.Application;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.CreditCard;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.APIContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.commercetools.service.paypalPlus.PaypalPlusUtil.*;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static java.util.Optional.of;
import static org.assertj.core.api.Java6Assertions.assertThat;

// TODO: move to separate integration test

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PaypalPlusPaymentServiceImplIntegrationTest {

    @Autowired
    private PaypalPlusPaymentService paymentService;

    /**
     * It is used to supply store credit card.
     */
    @Autowired
    private APIContext paypalPlusApiContext;

    @Test
    public void validatePaymentServiceContextInjection() {
        assertThat(paymentService).isExactlyInstanceOf(PaypalPlusPaymentServiceImpl.class);
    }

    @Test
    public void createPseudoCreditCardPayment() throws Exception {
        CreditCard dummyCreditCard = dummyCreditCard();
        final CreditCard storedCreditCard = dummyCreditCard.create(paypalPlusApiContext);

        Payment mockPayment = dummyCreditCardSecurePayment(storedCreditCard.getId());

        Payment payment = executeBlocking(paymentService.create(mockPayment));

        assertThat(payment).isNotNull();
        List<FundingInstrument> savedFundingInstruments = of(payment).map(Payment::getPayer).map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedFundingInstruments).isNotNull();
        assertThat(savedFundingInstruments.size()).isEqualTo(1);
        FundingInstrument savedFundingInstrument = savedFundingInstruments.get(0);
        assertThat(savedFundingInstrument.getCreditCardToken().getCreditCardId()).isEqualTo(storedCreditCard.getId());
        assertThat(savedFundingInstrument.getCreditCardToken().getLast4()).isEqualTo("0779");
    }


    @Test
    public void createCreditCardPayment() throws Exception {

        Payment mockPayment = dummyCreditCardSimplePayment();

        Payment payment = executeBlocking(paymentService.create(mockPayment));

        assertThat(payment).isNotNull();
        List<FundingInstrument> savedList = of(payment).map(Payment::getPayer).map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedList).isNotNull();
        assertThat(savedList.size()).isEqualTo(1);
        assertThat(savedList.get(0).getCreditCard().getNumber()).isEqualTo("xxxxxxxxxxxx0779");
    }

}