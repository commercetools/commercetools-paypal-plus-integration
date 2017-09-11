package com.commercetools.payment.constants;

import com.commercetools.payment.constants.ctp.CtpPaymentMethods;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods;

/**
 * CTP to Paypal Plus payment method names mapping.
 * <p>
 * So far the only accepted payment name by Paypal Plus in Europe is
 * <code>{@link PaypalPlusPaymentMethods#PAYPAL paypal}</code>, but might be extended in the future.
 * <p>
 * From CTP prospective now we have {@link CtpPaymentMethods#INSTALLMENT}, which has special workflow, and
 * {@link CtpPaymentMethods#DEFAULT} for the rest of payments.
 */
public enum CtpToPaypalPlusPaymentMethodsMapping {

    DEFAULT(CtpPaymentMethods.DEFAULT, PaypalPlusPaymentMethods.PAYPAL),
    INSTALLMENT(CtpPaymentMethods.INSTALLMENT, PaypalPlusPaymentMethods.PAYPAL);
    //CREDIT_CARD (, CREDIT_CARD) // according to Paypal Plus support - not used in Europe

    private final String ctpMethodName;
    private final String ppMethodName;

    CtpToPaypalPlusPaymentMethodsMapping(String ctpMethodName, String ppMethodName) {
        this.ctpMethodName = ctpMethodName;
        this.ppMethodName = ppMethodName;
    }

    public String getCtpMethodName() {
        return ctpMethodName;
    }

    public String getPpMethodName() {
        return ppMethodName;
    }
}
