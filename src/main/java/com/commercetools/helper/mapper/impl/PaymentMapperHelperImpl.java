package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.PaymentMapperHelper;
import com.commercetools.helper.mapper.impl.payment.DefaultPaymentMapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

@Component
public class PaymentMapperHelperImpl implements PaymentMapperHelper {

    private final Map<String, PaymentMapper> paymentMapperMap;

    private final PaymentMapper defaultPaymentMapper;

    /**
     * @param paymentMapperList        all available {@link PaymentMapper} implementations (they expected to be injected
     *                                 as beans).
     * @param defaultPaymentMapperImpl instance of default payment mapper implementation
     *                                 (although {@code paymentMapperList} could also contain this instance).
     */
    @Autowired
    public PaymentMapperHelperImpl(@Nonnull List<PaymentMapper> paymentMapperList,
                                   @Nonnull DefaultPaymentMapperImpl defaultPaymentMapperImpl) {
        paymentMapperMap = paymentMapperList.stream()
                .collect(toMap(paymentMapper -> paymentMapper.getCtpToPpPaymentMethodsMapping().getCtpMethodName(), i -> i));
        this.defaultPaymentMapper = defaultPaymentMapperImpl;
    }

    @Override
    public Optional<PaymentMapper> getPaymentMapper(@Nullable String ctpPaymentMethod) {
        return ofNullable(paymentMapperMap.get(ctpPaymentMethod));
    }

    @Nonnull
    @Override
    public PaymentMapper getPaymentMapperOrDefault(@Nullable String ctpPaymentMethod) {
        return getPaymentMapper(ctpPaymentMethod)
                .orElseGet(this::getDefaultPaymentMapper);
    }

    @Override
    public PaymentMapper getDefaultPaymentMapper() {
        return defaultPaymentMapper;
    }
}
