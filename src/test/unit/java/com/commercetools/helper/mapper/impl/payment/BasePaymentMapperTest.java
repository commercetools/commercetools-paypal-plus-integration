package com.commercetools.helper.mapper.impl.payment;

import com.paypal.api.payments.Details;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Transaction;
import io.sphere.sdk.carts.LineItem;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BasePaymentMapperTest {
    protected static Item getItemBySku(ItemList itemList, String sku) {
        return itemList.getItems().stream()
                .filter(item -> sku.equals(item.getSku()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Some items with the same SKU may be duplicated, because they have complex discounted price, thus one line item
     * is split to several. See
     * {@link DefaultPaymentMapperImpl#mapLineItemToPaypalPlusItem(LineItem, List)}
     */
    protected static Item getItemBySkuQuantityPrice(ItemList itemList, String sku, String quantity, String price) {
        return itemList.getItems().stream()
                .filter(item -> sku.equals(item.getSku()))
                .filter(item -> quantity.equals(item.getQuantity()))
                .filter(item -> price.equals(item.getPrice()))
                .findFirst()
                .orElse(null);
    }

    protected static void assertItem(Item item, String name, String quantity, String price, String currency) {
        assertItem(item, name, currency);
        assertThat(item.getQuantity()).isEqualTo(quantity);
        assertThat(item.getPrice()).isEqualTo(price);
    }

    protected static void assertItem(Item item, String name, String currency) {
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
    protected static void assertTransactionAmounts(Transaction transaction) {
        BigDecimal total = new BigDecimal(transaction.getAmount().getTotal());
        Details details = transaction.getAmount().getDetails();
        BigDecimal shippingCost = new BigDecimal(details.getShipping());
        BigDecimal detailsTotal = shippingCost
                .add(new BigDecimal(details.getSubtotal()));

        assertThat(detailsTotal).isEqualTo(total);

        BigDecimal totalLineItems = transaction.getItemList().getItems().stream()
                .map(item -> new BigDecimal(item.getPrice()).multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(totalLineItems.add(shippingCost)).isEqualTo(total);
    }
}
