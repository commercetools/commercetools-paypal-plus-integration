package com.commercetools.util;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RelatedResources;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static com.commercetools.util.PaypalPlusPaymentUtil.getFirstSaleTransactionFromPayment;
import static com.commercetools.util.PaypalPlusPaymentUtil.getFirstTransactionFromPayment;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class PaypalPlusPaymentUtilTest {


    @Test
    public void getFirstTransactionFromPayment_case() throws Exception {
        Payment payment = new Payment("test", null);
        assertThat(getFirstTransactionFromPayment(null)).isEmpty();
        assertThat(getFirstTransactionFromPayment(payment)).isEmpty();

        ArrayList<Transaction> transactions = new ArrayList<>();
        payment.setTransactions(transactions);
        assertThat(getFirstTransactionFromPayment(payment)).isEmpty();

        Transaction txn1 = new Transaction();
        transactions.add(txn1);
        assertThat(getFirstTransactionFromPayment(payment)).containsSame(txn1);

        // second transaction is ignored
        Transaction txn2 = new Transaction();
        transactions.add(txn2);
        assertThat(getFirstTransactionFromPayment(payment)).containsSame(txn1);

        // first is removed - second is returned
        transactions.remove(0);
        assertThat(getFirstTransactionFromPayment(payment)).containsSame(txn2);
    }

    @Test
    public void getFirstSaleTransactionIdFromPayment_case() throws Exception {
        Payment payment = new Payment("test", null);
        ArrayList<Transaction> transactions = new ArrayList<>();
        payment.setTransactions(transactions);

        Transaction txn1 = new Transaction();
        Transaction txn2 = new Transaction();
        transactions.add(txn1);
        transactions.add(txn2);

        assertThat(getFirstSaleTransactionFromPayment(payment)).isEmpty();

        ArrayList<RelatedResources> relatedResourcesList = new ArrayList<>();
        txn1.setRelatedResources(relatedResourcesList);
        assertThat(getFirstSaleTransactionFromPayment(payment)).isEmpty();

        RelatedResources relatedResources1 = new RelatedResources();
        RelatedResources relatedResources2 = new RelatedResources();
        relatedResourcesList.add(relatedResources1);
        relatedResourcesList.add(relatedResources2);
        assertThat(getFirstSaleTransactionFromPayment(payment)).isEmpty();

        Sale sale1 = new Sale();
        relatedResources1.setSale(sale1);
        assertThat(getFirstSaleTransactionFromPayment(payment)).containsSame(sale1);

        // second related resource should be ignored
        Sale sale2 = new Sale();
        relatedResources2.setSale(sale2);
        assertThat(getFirstSaleTransactionFromPayment(payment)).containsSame(sale1);
    }

}