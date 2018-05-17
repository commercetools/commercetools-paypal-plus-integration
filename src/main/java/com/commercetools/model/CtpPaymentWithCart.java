package com.commercetools.model;


import com.commercetools.payment.constants.ctp.CtpPaymentCustomFields;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.PaymentMethodInfo;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static com.commercetools.payment.constants.LocaleConstants.DEFAULT_LOCALE;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.util.CustomFieldUtil.*;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * Class aggregator for payment and related cart object. This object guarantees the payment and the cart are non-null.
 */
public class CtpPaymentWithCart {
    private final Payment payment;
    private final Cart cart;

    public CtpPaymentWithCart(@Nonnull Payment payment, @Nonnull Cart cart) {
        this.payment = payment;
        this.cart = cart;
    }

    @Nonnull
    public Payment getPayment() {
        return payment;
    }

    @Nonnull
    public Cart getCart() {
        return cart;
    }

    /**
     * Specific payment method name, like credit card, paypal, vorkasse and so on.
     * <p>
     * See {@link com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods}
     *
     * @return payment method name from {@link Payment#getPaymentMethodInfo()}
     */
    @Nonnull
    public String getPaymentMethod() {
        return Optional.of(payment)
                .map(Payment::getPaymentMethodInfo)
                .map(PaymentMethodInfo::getMethod)
                .orElse("");
    }

    /**
     * @return Return URL (success URL) value from
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#SUCCESS_URL_FIELD} custom field, if exists.
     * Otherwise return empty string.
     */
    @Nonnull
    public String getReturnUrl() {
        return getCustomFieldStringOrEmpty(payment, SUCCESS_URL_FIELD);
    }

    /**
     * @return Cancel URL value from
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#CANCEL_URL_FIELD} custom field, if exists.
     * Otherwise return empty string.
     */
    @Nonnull
    public String getCancelUrl() {
        return getCustomFieldStringOrEmpty(payment, CANCEL_URL_FIELD);
    }

    /**
     * @return Seller Paypal Plus
     * <a href="https://developer.paypal.com/docs/integration/direct/payment-experience/"><i>experience profile id</i>
     * </a> from {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#EXPERIENCE_PROFILE_ID}
     * custom field, if exists. Otherwise returns <b>null</b>.
     */
    @Nullable
    public String getExperienceProfileId() {
        return getCustomFieldStringOrNull(payment, EXPERIENCE_PROFILE_ID);
    }

    /**
     * @return enum key of {@link CtpPaymentCustomFields#SHIPPING_PREFERENCE SHIPPING_PREFERENCE} custom field
     * (if exist). This value should be use in
     * {@link com.paypal.api.ApplicationContext#shippingPreference Payment#applicationContext#shippingPreference}
     * @see <a href="https://developer.paypal.com/docs/api/orders/#definition-application_context">application_context#shipping_preference</a>
     */
    @Nullable
    public String getShippingPreference() {
        return getCustomFieldEnumKeyOrNull(payment, SHIPPING_PREFERENCE);
    }

    /**
     * Get transaction description for the payment from {@link CtpPaymentCustomFields#DESCRIPTION description}
     * or {@link CtpPaymentCustomFields#REFERENCE reference} custom fields.
     * The value is built the following order:<ol>
     * <li>entirely <code>{@link #payment}#custom#description</code> string value, if not <code><b>null</b></code></li>
     * <li>otherwise: String "<i>Reference: <code>${{@link #payment}#custom#reference}</code></i>"
     * (the reference is a mandatory custom field of the payment object)</li>
     * </ol>
     * <p>
     * The value is set to <a href="https://developer.paypal.com/docs/api/payments/#definition-transaction">Payment#transaction#description</a>
     * field.
     * <p>
     * <b>Note:</b> {@code Maximum length: 127} (according to PayPal Plus documentation.
     *
     * @return description of the payment/transaction.
     * @see <a href="https://developer.paypal.com/docs/api/payments/#definition-transaction">PayPal Plus Payment#transaction</a>
     */
    @Nonnull
    public String getTransactionDescription() {
        return getCustomFieldString(payment, DESCRIPTION)
                .orElseGet(() -> format("Reference: %s", getCustomFieldStringOrEmpty(payment, REFERENCE)));
    }

    /**
     * Fetch ordered non-empty list of locales. The locales order as:<ol>
     * <li>payment custom field
     * {@link com.commercetools.payment.constants.ctp.CtpPaymentCustomFields#LANGUAGE_CODE_FIELD} (if exists)</li>
     * <li>{@link Cart#getLocale()} (if exists)</li>
     * <li>default {@link com.commercetools.payment.constants.LocaleConstants#DEFAULT_LOCALE} (mandatory)</li>
     * </ol>
     * <p>
     * Every result item is significant, so if payment or cart miss the locale there is no empty entry in the list.
     *
     * @return ordered locales list from payment, cart and the default
     * {@link com.commercetools.payment.constants.LocaleConstants#DEFAULT_LOCALE}
     */
    @Nonnull
    public List<Locale> getLocalesWithDefault() {
        LinkedHashSet<Locale> result = new LinkedHashSet<>(3);
        getCustomFieldString(payment, LANGUAGE_CODE_FIELD)
                .filter(StringUtils::isNotBlank)
                .map(Locale::forLanguageTag)
                .ifPresent(result::add);

        ofNullable(cart.getLocale())
                .ifPresent(result::add);

        result.add(DEFAULT_LOCALE);

        return new ArrayList<>(result);
    }
}
