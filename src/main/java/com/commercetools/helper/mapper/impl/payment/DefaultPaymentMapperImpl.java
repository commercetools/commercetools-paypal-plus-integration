package com.commercetools.helper.mapper.impl.payment;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.AddressMapper;
import com.commercetools.helper.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping.DEFAULT;

/**
 * Default CTP to PP payment mapper.
 */
@Component
public class DefaultPaymentMapperImpl extends BasePaymentMapperImpl implements PaymentMapper {

    @Autowired
    public DefaultPaymentMapperImpl(@Nonnull PaypalPlusFormatter paypalPlusFormatter,
                                    @Nonnull AddressMapper addressMapper,
                                    @Nonnull ProductNameMapper productNameMapper) {
        super(paypalPlusFormatter, DEFAULT, addressMapper, productNameMapper);
    }
}
