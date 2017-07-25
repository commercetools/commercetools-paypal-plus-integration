package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.*;
import io.sphere.sdk.carts.CartLike;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.List;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Component
public class PaymentMapperImpl implements PaymentMapper {

    private final PaypalPlusFormatter paypalPlusFormatter;

    @Autowired
    public PaymentMapperImpl(@Nonnull PaypalPlusFormatter paypalPlusFormatter) {
        this.paypalPlusFormatter = paypalPlusFormatter;
    }

    @Override
    @Nullable
    public Payment ctpPaymentToPaypalPlus(@Nullable CtpPaymentWithCart paymentWithCartLike) {
        if (paymentWithCartLike == null) {
            return null;
        }

        final Payment mappedPayment = new Payment();

        mappedPayment.setCart(paymentWithCartLike.getCart().getId());
        mappedPayment.setIntent(SALE);
        mappedPayment.setPayer(getPayer(paymentWithCartLike));
        mappedPayment.setTransactions(getTransactions(paymentWithCartLike));
        mappedPayment.setRedirectUrls(getRedirectUrls(paymentWithCartLike));

        return mappedPayment;
    }

    @Nonnull
    protected Payer getPayer(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new Payer()
                .setFundingInstruments(getFundingInstrumentList(paymentWithCartLike))
                // TODO: or always "paypal"?
                .setPaymentMethod(paymentWithCartLike.getPaymentMethod());
    }

    @Nonnull
    protected List<Transaction> getTransactions(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return singletonList(getTransaction(paymentWithCartLike));
    }

    @Nonnull
    protected RedirectUrls getRedirectUrls(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new RedirectUrls();
    }

    @Nonnull
    protected List<FundingInstrument> getFundingInstrumentList(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return singletonList(new FundingInstrument()
                .setCreditCardToken(getCreditCardToken(paymentWithCartLike)));
    }

    @Nonnull
    protected CreditCardToken getCreditCardToken(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new CreditCardToken(paymentWithCartLike.getCreditCardToken());
    }

    @Nonnull
    protected Transaction getTransaction(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        Transaction transaction = new Transaction();
        transaction.setAmount(getTransactionAmount(paymentWithCartLike));
        transaction.setDescription(getTransactionDescription(paymentWithCartLike));
        transaction.setItemList(getTransactionItemList(paymentWithCartLike));
        return transaction;
    }

    @Nonnull
    protected Amount getTransactionAmount(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        final CartLike cartLike = paymentWithCartLike.getCart();
//        MonetaryAmount totalPrice = cartLike.getTotalPrice();

        // looks like could be skipped, in this case it won't be validated against amount.total
//        Details details = new Details()
//                .setShipping()
//                .setSubtotal()
//                .setTax();
//
        final MonetaryAmount totalPrice = cartLike.getTotalPrice();
        return new Amount()
                .setCurrency(totalPrice.getCurrency().getCurrencyCode())
                // Total must be equal to the sum of shipping, tax and subtotal, if they are specified
                .setTotal(paypalPlusFormatter.monetaryAmountToString(totalPrice))
//                .setDetails(details);
                ;
    }

    @Nonnull
    protected String getTransactionDescription(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return "Payment from commercetools Paypal Plus integration service";
    }

    @Nonnull
    protected ItemList getTransactionItemList(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return new ItemList()
                .setItems(getLineItems(paymentWithCartLike));
    }

    /**
     * So far not implemented, but might be required in the future for some methods like
     *
     * @param paymentWithCartLike
     * @return empty list so far
     */
    @Nonnull
    protected List<Item> getLineItems(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        // TODO: fill line items, because they are mandatory
        return emptyList();
    }
}
