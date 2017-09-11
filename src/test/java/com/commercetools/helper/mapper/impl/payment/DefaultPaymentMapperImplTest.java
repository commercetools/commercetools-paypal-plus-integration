package com.commercetools.helper.mapper.impl.payment;

import com.commercetools.Application;
import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getPaymentWithCart_complexAndDiscount;
import static com.commercetools.testUtil.ctpUtil.CtpResourcesUtil.getPaymentWithCart_complexWithoutDiscount;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class DefaultPaymentMapperImplTest extends BasePaymentMapperTest {

    @Autowired
    private DefaultPaymentMapperImpl paymentMapper;

    @Test
    public void ctpPaymentToPaypalPlus_withDiscount() throws Exception {
        CtpPaymentWithCart paymentWithCart = getPaymentWithCart_complexAndDiscount();
        Payment ppPayment = paymentMapper.ctpPaymentToPaypalPlus(paymentWithCart);

        assertThat(ppPayment).isNotNull();

        assertThat(ppPayment.getIntent()).isEqualTo(SALE);

        assertThat(ppPayment.getPayer()).isNotNull();
        assertThat(ppPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);
        assertThat(ppPayment.getPayer().getExternalSelectedFundingInstrumentType())
                .withFailMessage(format("ExternalSelectedFundingInstrumentType must be empty for default Paypal payment, but [%s] found", ppPayment.getPayer().getExternalSelectedFundingInstrumentType()))
                .isNullOrEmpty();
        assertThat(ppPayment.getPayer().getPayerInfo())
                .withFailMessage(format("PayerInfo must be empty for default Paypal payment, but [%s] found", ppPayment.getPayer().getPayerInfo()))
                .isNull();

        assertThat(ppPayment.getState()).isNull();

        assertThat(ppPayment.getRedirectUrls().getReturnUrl()).isEqualTo("https://www.sparta.de/success/12333456");
        assertThat(ppPayment.getRedirectUrls().getCancelUrl()).isEqualTo("https://www.sparta.de/cancel/12333456");

        Transaction transaction = ppPayment.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("Payment from commercetools Paypal Plus integration service");

        Amount amount = transaction.getAmount();

        assertThat(amount.getCurrency()).isEqualTo("USD");
        assertThat(amount.getTotal()).isEqualTo("596.85");

        Details details = amount.getDetails();
        assertThat(details.getSubtotal()).isEqualTo("591.90");
        assertThat(details.getShipping()).isEqualTo("4.95");

        ItemList itemList = transaction.getItemList();
        assertThat(itemList).isNotNull();
        assertThat(itemList.getShippingAddress())
                .withFailMessage(format("Shipping address for default Paypal payment must be empty, but [%s] found", itemList.getShippingAddress()))
                .isNull();

        assertThat(itemList.getItems().size()).isEqualTo(5);
        assertItem(getItemBySkuQuantityPrice(itemList, "123454323454667", "1", "115.24"), "Halskette", "1", "115.24", "USD");
        assertItem(getItemBySkuQuantityPrice(itemList, "123454323454667", "3", "115.24"), "Halskette", "3", "115.24", "USD");
        assertItem(getItemBySku(itemList, "2345234523"), "Kasten", "1", "0.00", "USD");
        assertItem(getItemBySkuQuantityPrice(itemList, "456786468866578", "2", "43.65"), "Ringe", "USD");
        assertItem(getItemBySkuQuantityPrice(itemList, "456786468866578", "1", "43.64"), "Ringe", "USD");

        assertTransactionAmounts(transaction);
    }

    @Test
    public void ctpPaymentToPaypalPlus_withoutDiscount() throws Exception {
        CtpPaymentWithCart paymentWithCart = getPaymentWithCart_complexWithoutDiscount();
        Payment ppPayment = paymentMapper.ctpPaymentToPaypalPlus(paymentWithCart);

        assertThat(ppPayment).isNotNull();

        assertThat(ppPayment.getIntent()).isEqualTo(SALE);

        assertThat(ppPayment.getPayer()).isNotNull();
        assertThat(ppPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);
        assertThat(ppPayment.getPayer().getExternalSelectedFundingInstrumentType())
                .withFailMessage(format("ExternalSelectedFundingInstrumentType must be empty for default Paypal payment, but [%s] found", ppPayment.getPayer().getExternalSelectedFundingInstrumentType()))
                .isNullOrEmpty();
        assertThat(ppPayment.getPayer().getPayerInfo())
                .withFailMessage(format("PayerInfo must be empty for default Paypal payment, but [%s] found", ppPayment.getPayer().getPayerInfo()))
                .isNull();

        assertThat(ppPayment.getState()).isNull();

        assertThat(ppPayment.getRedirectUrls().getReturnUrl()).isEqualTo("https://www.sparta.de/success/556677884433");
        assertThat(ppPayment.getRedirectUrls().getCancelUrl()).isEqualTo("https://www.sparta.de/cancel/556677884433");

        Transaction transaction = ppPayment.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("Payment from commercetools Paypal Plus integration service");

        Amount amount = transaction.getAmount();
        assertThat(amount.getCurrency()).isEqualTo("EUR");
        assertThat(amount.getTotal()).isEqualTo("309.00");

        Details details = amount.getDetails();
        assertThat(details.getSubtotal()).isEqualTo("309.00");
        assertThat(details.getShipping()).isEqualTo("0.00");

        ItemList itemList = transaction.getItemList();
        assertThat(itemList).isNotNull();

        assertThat(itemList.getShippingAddress())
                .withFailMessage(format("Shipping address for default Paypal payment must be empty, but [%s] found", itemList.getShippingAddress()))
                .isNull();

        assertThat(itemList.getItems().size()).isEqualTo(3);
        assertItem(getItemBySku(itemList, "123456"), "Necklace Swarovski", "1", "129.00", "EUR");
        assertItem(getItemBySku(itemList, "776655"), "Every piece", "1", "0.00", "EUR");
        assertItem(getItemBySku(itemList, "998877665544"), "Earrings", "4", "45.00", "EUR");

        assertTransactionAmounts(transaction);
    }

}