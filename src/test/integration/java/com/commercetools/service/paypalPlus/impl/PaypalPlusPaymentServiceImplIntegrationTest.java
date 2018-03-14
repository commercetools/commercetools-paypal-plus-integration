package com.commercetools.service.paypalPlus.impl;

import com.commercetools.Application;
import com.commercetools.pspadapter.APIContextFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentLinkRel.APPROVAL_URL;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.CREDIT_CARD;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates.APPROVED;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates.CREATED;
import static com.commercetools.service.paypalPlus.PaypalPlusPaymentTestUtil.*;
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
    private APIContextFactory apiContextFactory;

    @Test
    public void validatePaymentServiceContextInjection() {
        assertThat(paymentService).isExactlyInstanceOf(PaypalPlusPaymentServiceImpl.class);
    }

    @Test
    public void createPseudoCreditCardPayment() throws Exception {
        CreditCard dummyCreditCard = dummyCreditCard();
        final CreditCard storedCreditCard = dummyCreditCard.create(apiContextFactory.createAPIContext());

        Payment mockPayment = dummyCreditCardSecurePayment(storedCreditCard.getId());

        Payment savedPayment = executeBlocking(paymentService.create(mockPayment));

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotBlank();
        assertThat(savedPayment.getState()).isEqualTo(APPROVED);
        assertThat(savedPayment.getPayer().getPaymentMethod()).isEqualTo(CREDIT_CARD);
        List<FundingInstrument> savedFundingInstruments = of(savedPayment).map(Payment::getPayer)
                .map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedFundingInstruments).isNotNull();
        assertThat(savedFundingInstruments.size()).isEqualTo(1);
        FundingInstrument savedFundingInstrument = savedFundingInstruments.get(0);
        assertThat(savedFundingInstrument.getCreditCardToken().getCreditCardId()).isEqualTo(storedCreditCard.getId());
        assertThat(savedFundingInstrument.getCreditCardToken().getLast4()).isEqualTo("0779");
    }


    @Test
    public void createCreditCardPayment() throws Exception {
        Payment mockPayment = dummyCreditCardSimplePayment();

        Payment savedPayment = executeBlocking(paymentService.create(mockPayment));

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotBlank();
        assertThat(savedPayment.getState()).isEqualTo(APPROVED);
        assertThat(savedPayment.getPayer().getPaymentMethod()).isEqualTo(CREDIT_CARD);
        List<FundingInstrument> savedList = of(savedPayment).map(Payment::getPayer).map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedList).isNotNull();
        assertThat(savedList.size()).isEqualTo(1);
        assertThat(savedList.get(0).getCreditCard().getNumber()).isEqualTo("xxxxxxxxxxxx0779");
    }

    @Test
    public void createPaypalPayment() throws Exception {
        Payment dummyPayment = dummyPaypalPayment();

        Payment savedPayment = executeBlocking(paymentService.create(dummyPayment));

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotBlank();
        assertThat(savedPayment.getState()).isEqualTo(CREATED);
        assertThat(savedPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);

        Item soldItem = savedPayment.getTransactions().stream()
                .map(tr -> tr.getItemList().getItems().stream().findFirst().orElse(null))
                .findFirst().orElse(null);
        assertThat(soldItem).isNotNull();
        assertThat(soldItem.getName()).isEqualTo("Ground Coffee 40 oz");
        assertThat(soldItem.getPrice()).isEqualTo("5.33");

        // verify redirect URL
        String approvalUrl = savedPayment.getLinks().stream()
                .filter(link -> APPROVAL_URL.equals(link.getRel()))
                .map(Links::getHref)
                .findFirst()
                .orElse("");

        assertThat(approvalUrl).startsWith("https://www.sandbox.paypal.com/");
    }

}