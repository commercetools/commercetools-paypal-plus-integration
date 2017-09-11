package com.commercetools.helper.mapper.impl.payment;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.AddressMapper;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.ShippingAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping.INSTALLMENT;
import static java.util.Optional.ofNullable;

/**
 * Payment mapper for {@link CtpToPaypalPlusPaymentMethodsMapping#INSTALLMENT} payment types.
 * <p>
 * This implementation overrides <i>payer info</i> and <i>external selected funding instrument type</i>
 * (<code>Credit</code>) of the payment as specified in <i>Integration Requirements for Installments Germany.
 * 2nd Button Integration</i> documentation (Chapter 3.1.1. Create a Payment).
 */
@Component
public class InstallmentPaymentMapperImpl extends BasePaymentMapperImpl implements PaymentMapper {

    public static final String CREDIT = "Credit";

    @Autowired
    public InstallmentPaymentMapperImpl(@Nonnull PaypalPlusFormatter paypalPlusFormatter,
                                        @Nonnull AddressMapper addressMapper) {
        super(paypalPlusFormatter, INSTALLMENT, addressMapper);
    }

    @Override
    @Nullable
    protected String getExternalSelectedFundingInstrumentType(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return CREDIT;
    }

    @Override
    @Nullable
    protected PayerInfo getPayerInfo(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return ofNullable(paymentWithCartLike.getCart().getBillingAddress())
                .map(addressMapper::ctpAddressToPaypalPlusPayerInfo)
                .orElse(null);
    }

    @Nullable
    @Override
    protected ShippingAddress getItemListShippingAddress(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        return ofNullable(paymentWithCartLike.getCart().getShippingAddress())
                .map(addressMapper::ctpAddressToPaypalPlusShippingAddress)
                .orElse(null);
    }
}
