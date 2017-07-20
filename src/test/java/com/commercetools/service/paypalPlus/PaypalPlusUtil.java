package com.commercetools.service.paypalPlus;

import com.paypal.api.payments.*;

import java.util.List;

import static java.util.Collections.singletonList;

public final class PaypalPlusUtil {

    /**
     * @return new instance of dummy payment with explicit credit card credentials.
     */
    public static Payment dummyCreditCardSimplePayment() {
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

        return new Payment()
                .setIntent("sale")
                .setPayer(payer)
                .setTransactions(transactions);
    }

    /**
     * @param creditCardTokenId secure token of preliminary stored customer's credit card.
     * @return new instance of dummy payment where credit card is specified over the token.
     */
    public static Payment dummyCreditCardSecurePayment(String creditCardTokenId) {
        CreditCardToken creditCardToken = new CreditCardToken(creditCardTokenId);

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

        return new Payment()
                .setIntent("sale")
                .setPayer(payer)
                .setTransactions(transactions);
    }

    /**
     * @return dummy {@link CreditCard} instance to use in Paypal Plus payment.
     */
    public static CreditCard dummyCreditCard() {
        return new CreditCard()
                .setType("visa")
                .setNumber("4669424246660779")
                .setExpireMonth(11)
                .setExpireYear(2019)
                .setCvv2("012")
                .setFirstName("Joe")
                .setLastName("Shopper");
    }

    private PaypalPlusUtil() {
    }
}
