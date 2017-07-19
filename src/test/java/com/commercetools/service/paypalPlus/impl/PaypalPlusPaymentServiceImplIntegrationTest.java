package com.commercetools.service.paypalPlus.impl;

import com.commercetools.Application;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.assertj.core.api.Java6Assertions.assertThat;

// TODO: move to separate integration test

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class PaypalPlusPaymentServiceImplIntegrationTest {

    @Autowired
    private PaypalPlusPaymentService paymentService;

    @Autowired
    private APIContext paypalPlusApiContext;

    @Test
    public void validatePaymentServiceContextInjection() {
        assertThat(paymentService).isExactlyInstanceOf(PaypalPlusPaymentServiceImpl.class);
    }

    @Test
    public void createPseudoCreditCardPayment() throws Exception {
        CreditCard card = new CreditCard()
                .setType("visa")
                .setNumber("4669424246660779")
                .setExpireMonth(11)
                .setExpireYear(2019)
                .setCvv2("012")
                .setFirstName("Joe")
                .setLastName("Shopper");

        final CreditCard storedCreditCard = card.create(paypalPlusApiContext);

        CreditCardToken creditCardToken = new CreditCardToken(storedCreditCard.getId());

        Details details = new Details()
                .setShipping("1.01")
                .setSubtotal("5.23")
                .setTax("1.99");

        Amount amount = new Amount()
                .setCurrency("USD")
                .setTotal("8.23") // Total must be equal to the sum of shipping, tax and subtotal.
                .setDetails(details);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");

        List<Transaction> transactions = singletonList(transaction);

        FundingInstrument fundingInstrument = new FundingInstrument()
                .setCreditCardToken(creditCardToken);

        List<FundingInstrument> fundingInstrumentList = singletonList(fundingInstrument);

        Payer payer = new Payer()
                .setFundingInstruments(fundingInstrumentList)
                .setPaymentMethod("credit_card");

        Payment mockPayment = new Payment()
                .setIntent("sale")
                .setPayer(payer)
                .setTransactions(transactions);


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
        Address billingAddress = new Address();
        billingAddress.setCity("Johnstown");
        billingAddress.setCountryCode("US");
        billingAddress.setLine1("52 N Main ST");
        billingAddress.setPostalCode("43210");
        billingAddress.setState("OH");

        CreditCard creditCard = new CreditCard()
                .setBillingAddress(billingAddress)
                .setCvv2("012")
                .setExpireMonth(11)
                .setExpireYear(2018)
                .setFirstName("Joe")
                .setLastName("Shopper")
                .setNumber("4669424246660779")
                .setType("visa");

        Details details = new Details()
                .setShipping("1.23")
                .setSubtotal("6.10")
                .setTax("1.45");

        Amount amount = new Amount()
                .setCurrency("USD")
                .setTotal("8.78") // Total must be equal to sum of shipping, tax and subtotal.
                .setDetails(details);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");

        List<Transaction> transactions = singletonList(transaction);

        FundingInstrument fundingInstrument = new FundingInstrument()
                .setCreditCard(creditCard);

        List<FundingInstrument> fundingInstrumentList = singletonList(fundingInstrument);

        Payer payer = new Payer()
                .setFundingInstruments(fundingInstrumentList)
                .setPaymentMethod("credit_card");

        Payment mockPayment = new Payment()
                .setIntent("sale")
                .setPayer(payer)
                .setTransactions(transactions);

        Payment payment = executeBlocking(paymentService.create(mockPayment));

        assertThat(payment).isNotNull();
        List<FundingInstrument> savedList = of(payment).map(Payment::getPayer).map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedList).isNotNull();
        assertThat(savedList.size()).isEqualTo(1);
        assertThat(savedList.get(0).getCreditCard().getNumber()).isEqualTo("xxxxxxxxxxxx0779");
    }

}