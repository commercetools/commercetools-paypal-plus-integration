package com.commercetools.helper.mapper.impl;

import com.commercetools.Application;
import com.commercetools.model.CtpPaymentWithCart;
import com.paypal.api.payments.*;
import io.sphere.sdk.carts.LineItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentIntent.SALE;
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
    public void ctpPaymentToPaypalPlus_withDiscount() throws Exception {
        CtpPaymentWithCart paymentWithCart = getPaymentWithCart_complexAndDiscount();
        Payment ppPayment = paymentMapper.ctpPaymentToPaypalPlus(paymentWithCart);

        assertThat(ppPayment).isNotNull();

        assertThat(ppPayment.getIntent()).isEqualTo(SALE);

        assertThat(ppPayment.getPayer()).isNotNull();
        assertThat(ppPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);

        assertThat(ppPayment.getState()).isNull();

        assertThat(ppPayment.getRedirectUrls().getReturnUrl()).isEqualTo("https://www.sparta.de/success/12333456");
        assertThat(ppPayment.getRedirectUrls().getCancelUrl()).isEqualTo("https://www.sparta.de/cancel/12333456");

        Transaction transaction = ppPayment.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("Payment from commercetools Paypal Plus integration service");

        Amount amount = transaction.getAmount();

        assertThat(amount.getCurrency()).isEqualTo("USD");
        assertThat(amount.getTotal()).isEqualTo("596.85");

        assertThat(transaction.getAmount().getDetails())
                .withFailMessage("If you see this message - you should update the tests to validate payment details")
                .isNull();
//        Details details = amount.getDetails();
//        assertThat(details.getSubtotal()).isEqualTo("496.60");
//        assertThat(details.getShipping()).isEqualTo("4.95");
//        assertThat(details.getTax()).isEqualTo("95.30");

        ItemList itemList = transaction.getItemList();
        assertThat(itemList).isNotNull();
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

        assertThat(ppPayment.getState()).isNull();

        assertThat(ppPayment.getRedirectUrls().getReturnUrl()).isEqualTo("https://www.sparta.de/success/556677884433");
        assertThat(ppPayment.getRedirectUrls().getCancelUrl()).isEqualTo("https://www.sparta.de/cancel/556677884433");

        Transaction transaction = ppPayment.getTransactions().get(0);
        assertThat(transaction.getDescription()).isEqualTo("Payment from commercetools Paypal Plus integration service");

        Amount amount = transaction.getAmount();
        assertThat(amount.getCurrency()).isEqualTo("EUR");
        assertThat(amount.getTotal()).isEqualTo("309.00");

        assertThat(transaction.getAmount().getDetails())
                .withFailMessage("If you see this message - you should update the tests to validate payment details")
                .isNull();
//        Details details = amount.getDetails();
//        assertThat(details.getSubtotal()).isEqualTo("259.66");
//        assertThat(details.getShipping()).isEqualTo("0.00");
//        assertThat(details.getTax()).isEqualTo("49.34");

        ItemList itemList = transaction.getItemList();
        assertThat(itemList).isNotNull();
        assertThat(itemList.getItems().size()).isEqualTo(3);
        assertItem(getItemBySku(itemList, "123456"), "Necklace Swarovski", "1", "129.00", "EUR");
        assertItem(getItemBySku(itemList, "776655"), "Every piece", "1", "0.00", "EUR");
        assertItem(getItemBySku(itemList, "998877665544"), "Earrings", "4", "45.00", "EUR");

        assertTransactionAmounts(transaction);
    }

    private Item getItemBySku(ItemList itemList, String sku) {
        return itemList.getItems().stream()
                .filter(item -> sku.equals(item.getSku()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Some items with the same SKU may be duplicated, because they have complex discounted price, thus one line item
     * is split to several. See
     * {@link PaymentMapperImpl#mapLineItemToPaypalPlusItem(LineItem, List)}
     */
    private Item getItemBySkuQuantityPrice(ItemList itemList, String sku, String quantity, String price) {
        return itemList.getItems().stream()
                .filter(item -> sku.equals(item.getSku()))
                .filter(item -> quantity.equals(item.getQuantity()))
                .filter(item -> price.equals(item.getPrice()))
                .findFirst()
                .orElse(null);
    }

    private void assertItem(Item item, String name, String quantity, String price, String currency) {
        assertItem(item, name, currency);
        assertThat(item.getQuantity()).isEqualTo(quantity);
        assertThat(item.getPrice()).isEqualTo(price);
    }

    private void assertItem(Item item, String name, String currency) {
        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo(name);
        assertThat(item.getCurrency()).isEqualTo(currency);
    }

    /**
     * Validate:<ul>
     * <li>{@link Amount#details} sum is equal to {@link Amount#total}</li>
     * <li>{@link Transaction#getItemList()} (prices * quantity) sum equal to {@link Amount#total}</li>
     * </ul>
     * <b>Note:</b> the test is temporary disabled while
     * <a href="https://github.com/commercetools/commercetools-paypal-plus-integration/issues/28">Paypal Plus taxes
     * counting issue</a> is not solved.
     *
     * @param transaction {@link Transaction} to validate.
     */
    private void assertTransactionAmounts(Transaction transaction) {
        assertThat(transaction.getAmount().getDetails())
                .withFailMessage("If you see this message - you should update the tests to validate payment details")
                .isNull();

//        BigDecimal total = new BigDecimal(transaction.getAmount().getTotal());
//        Details details = transaction.getAmount().getDetails();
//        BigDecimal shippingCost = new BigDecimal(details.getShipping());
//        BigDecimal detailsTotal = shippingCost
//                .add(new BigDecimal(details.getTax()))
//                .add(new BigDecimal(details.getSubtotal()));
//
//        assertThat(detailsTotal).isEqualTo(total);
//
//        BigDecimal totalLineItems = transaction.getItemList().getItems().stream()
//                .map(item -> new BigDecimal(item.getPrice()).multiply(new BigDecimal(item.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        assertThat(totalLineItems.add(shippingCost)).isEqualTo(total);
    }

}