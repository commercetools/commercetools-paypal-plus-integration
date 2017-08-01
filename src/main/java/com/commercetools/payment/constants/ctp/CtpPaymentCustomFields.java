package com.commercetools.payment.constants.ctp;

public final class CtpPaymentCustomFields {

    public static final String SUCCESS_URL_FIELD = "successUrl";
    public static final String CANCEL_URL_FIELD = "cancelUrl";
    public static final String APPROVAL_URL = "approvalUrl";

    public static final String LANGUAGE_CODE_FIELD = "languageCode";
    public static final String REFERENCE = "reference";

    /**
     * <b>Note:</b> looks like for Paypal Plus in EU credit card token is never shared with payers, cos all the workflow
     * is executed in the iframe.
     */
    public static final String CREDIT_CARD_TOKEN = "cardDataPlaceholder";

    private CtpPaymentCustomFields() {
    }
}
