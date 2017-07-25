package com.commercetools.service.paypalPlus;

import com.paypal.api.payments.*;

import java.util.List;
import java.util.UUID;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.CREDIT_CARD;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static java.util.Collections.singletonList;

public final class PaypalPlusPaymentTestUtil {

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
                .setPaymentMethod(CREDIT_CARD);

        return new Payment()
                .setIntent(SALE)
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
                .setPaymentMethod(CREDIT_CARD);

        return new Payment()
                .setIntent(SALE)
                .setPayer(payer)
                .setTransactions(transactions);
    }

    public static Payment dummyPaypalPayment() {
        Details details = new Details()
                .setShipping("3.22")
                .setSubtotal("5.33")
                .setTax("1.44");

        Amount amount = new Amount()
                .setCurrency("USD")
                .setTotal("9.99")
                .setDetails(details);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("This is the payment transaction description.");

        Item item = new Item()
                .setName("Ground Coffee 40 oz")
                .setQuantity("1")
                .setCurrency("USD")
                .setPrice("5.33");

        ItemList itemList = new ItemList();
        itemList.setItems(singletonList(item));

        transaction.setItemList(itemList);

        List<Transaction> transactions = singletonList(transaction);

        Payer payer = new Payer()
                .setPaymentMethod(PAYPAL);

        Payment payment = new Payment()
                .setIntent(SALE)
                .setPayer(payer)
                .setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        String guid = UUID.randomUUID().toString().replaceAll("-", "");
        redirectUrls.setCancelUrl("http://example.com/paymentwithpaypal/cancel?guid=" + guid);
        redirectUrls.setReturnUrl("http://example.com/paymentwithpaypal/return?guid=" + guid);
        payment.setRedirectUrls(redirectUrls);

        return payment;
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

    private PaypalPlusPaymentTestUtil() {
    }
}
