package com.commercetools.model;


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
