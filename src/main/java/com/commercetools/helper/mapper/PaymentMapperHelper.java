package com.commercetools.helper.mapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public interface PaymentMapperHelper {

    /**
     * Find payment mapper by CTP payment name.
     *
     * @param ctpPaymentMethod CTP payment method name, expected to be one of
     *                         {@link com.commercetools.payment.constants.ctp.CtpPaymentMethods}.
     * @return Optional {@link PaymentMapper}, if such implementation exists for specified {@code ctpPaymentMethod}
     */
    Optional<PaymentMapper> getPaymentMapper(@Nullable String ctpPaymentMethod);

    /**
     * Find payment mapper by CTP payment name, or return default if name is not specified.
     *
     * @param ctpPaymentMethod CTP payment method name, expected to be one of
     *                         {@link com.commercetools.payment.constants.ctp.CtpPaymentMethods}
     * @return {@link PaymentMapper} associated with {@code ctpPaymentMethod} or default mapping implementation.
     */
    @Nonnull
    PaymentMapper getPaymentMapperOrDefault(@Nullable String ctpPaymentMethod);

    /**
     * @return Payment mapper which is used for default payments.
     */
    @Nonnull
    PaymentMapper getDefaultPaymentMapper();
}
