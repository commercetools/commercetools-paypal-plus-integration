package com.commercetools.payment;

import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.pspadapter.tenant.TenantProperties;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.CartDraft;
import io.sphere.sdk.carts.CartDraftBuilder;
import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.AddPayment;
import io.sphere.sdk.client.BlockingSphereClient;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentDraftBuilder;
import io.sphere.sdk.payments.PaymentDraftDsl;
import io.sphere.sdk.payments.PaymentMethodInfoBuilder;
import io.sphere.sdk.payments.commands.PaymentCreateCommand;
import io.sphere.sdk.types.CustomFieldsDraftBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupOrdersCartsPayments;
import static com.commercetools.testUtil.ctpUtil.SphereClientTestUtil.getBlockingSphereClient;
import static com.commercetools.testUtil.ctpUtil.SphereClientTestUtil.getFirstTenantEntry;
import static com.commercetools.testUtil.ctpUtil.UsecasesCtpResourcesUtil.getUTDummyComplexCartDraftWithDiscounts;
import static io.sphere.sdk.models.DefaultCurrencyUnits.EUR;
import static java.util.Optional.ofNullable;

// simplify reading configuration properties using Spring configuration feature:
// so we could supply client properties in the same way, as for the application and integration tests.
// But don't run entire application context!
@SpringBootTest(classes = TenantProperties.class)
@EnableConfigurationProperties
public class BasePaymentUT {

    protected BlockingSphereClient sphereClient;

    /**
     * Must be the same, as in the running service (docker container)
     */
    protected String tenantKey;

    /**
     * Use it internally only to initialize {@link #sphereClient}
     */
    @Autowired
    private TenantProperties tenantProperties;

    /**
     * <ol>
     * <li>Cleanup orders, carts and payments storages.</li>
     * <li>Create CTP payment custom types</li>
     * </ol>
     */
    public void setupBeforeAll() {
        initTenantConfigs();
        cleanupOrdersCartsPayments(sphereClient);
    }

    /**
     * Cleanup CTP payment custom types crated in {@link #setupBeforeAll()}.
     * Close client since it is not cacheable/reusable as in the application itself.
     */
    public void tearDownAfterAll() {
        if (sphereClient != null) {
            cleanupOrdersCartsPayments(sphereClient);
            sphereClient.close();
            sphereClient = null;
        }
    }

    public void setUp() {
        initTenantConfigs();
    }

    /**
     * Instantiate {@link #sphereClient} from first (in alphabetical order) tenant properties
     * supplied in {@link #tenantProperties}
     */
    protected void initTenantConfigs() {
        Map.Entry<String, TenantProperties.Tenant> tenantEntry = getFirstTenantEntry(tenantProperties);
        tenantKey = tenantEntry.getKey();
        sphereClient = getBlockingSphereClient(tenantEntry.getValue());
    }

    protected String createCartAndPayment(@Nonnull SphereClient sphereClient) {
        Cart updatedCart = executeBlocking(createCartCS(sphereClient)
                .thenCompose(cart -> createPaymentCS(cart.getTotalPrice(), cart.getLocale(), sphereClient)
                        .thenApply(payment -> new CtpPaymentWithCart(payment, cart))
                        .thenCompose(ctpPaymentWithCart -> sphereClient.execute(CartUpdateCommand.of(ctpPaymentWithCart.getCart(),
                                AddPayment.of(ctpPaymentWithCart.getPayment()))))));

        return updatedCart.getPaymentInfo().getPayments().get(0).getId();
    }

    public static CompletionStage<Cart> createCartCS(@Nonnull SphereClient sphereClient) {
        CartDraft dummyComplexCartWithDiscounts = CartDraftBuilder.of(getUTDummyComplexCartDraftWithDiscounts())
                .currency(EUR)
                .build();
        return sphereClient.execute(CartCreateCommand.of(dummyComplexCartWithDiscounts));
    }

    public static CompletionStage<Payment> createPaymentCS(@Nonnull MonetaryAmount totalPrice,
                                                           Locale locale,
                                                           SphereClient sphereClient) {
        PaymentDraftDsl dsl = createPaymentDraftBuilder(totalPrice, locale)
                .build();
        return sphereClient.execute(PaymentCreateCommand.of(dsl));
    }

    public static PaymentDraftBuilder createPaymentDraftBuilder(@Nonnull MonetaryAmount totalPrice, @Nullable Locale locale) {
        return PaymentDraftBuilder.of(totalPrice)
                .paymentMethodInfo(PaymentMethodInfoBuilder.of().paymentInterface(PAYPAL_PLUS).method(DEFAULT).build())
                .custom(CustomFieldsDraftBuilder.ofTypeKey("payment-paypal")
                        .addObject(SUCCESS_URL_FIELD, "http://example.com/test-usecases/success/23456789")
                        .addObject(CANCEL_URL_FIELD, "http://example.com/test-usecases/cancel/23456789")
                        .addObject(REFERENCE, "test-usecases-reference")
                        .addObject(LANGUAGE_CODE_FIELD, ofNullable(locale).orElse(DEFAULT_LOCALE).getLanguage())
                        .build());
    }
}