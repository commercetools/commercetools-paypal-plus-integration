package com.commercetools.helper.mapper.impl.payment;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping;
import com.paypal.api.payments.PayerInfo;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.commercetools.payment.constants.CtpToPaypalPlusPaymentMethodsMapping.INSTALLMENT;

/**
 * Payment mapper for {@link CtpToPaypalPlusPaymentMethodsMapping#INSTALLMENT} payment types.
 * <p>
 * This implementation overrides <i>payer info</i> and <i>external selected funding instrument type</i>
 * (<code>Credit</code>) of the payment as specified in <i>Integration Requirements for Installments Germany.
 * 2nd Button Integration</i> documentation (Chapter 3.1.1. Create a Payment).
 */
@Component
public class InstallmentPaymentMapperImpl extends BasePaymentMapperImpl implements PaymentMapper {

    @Autowired
    public InstallmentPaymentMapperImpl(@Nonnull PaypalPlusFormatter paypalPlusFormatter) {
        super(paypalPlusFormatter, INSTALLMENT);
    }

    @Override
    @Nullable
    protected String getExternalSelectedFundingInstrumentType(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        throw new NotImplementedException("Implement first");
    }

    @Override
    @Nullable
    protected PayerInfo getPayerInfo(@Nonnull CtpPaymentWithCart paymentWithCartLike) {
        // TODO: finalize address
        //return new PayerInfo();
        throw new NotImplementedException("Implement first");
    }
}
