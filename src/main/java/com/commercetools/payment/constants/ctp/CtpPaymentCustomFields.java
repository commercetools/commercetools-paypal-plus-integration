package com.commercetools.payment.constants.ctp;

public final class CtpPaymentCustomFields {

    public static final String SUCCESS_URL_FIELD = "successUrl";
    public static final String CANCEL_URL_FIELD = "cancelUrl";
    public static final String APPROVAL_URL = "approvalUrl";
    public static final String PAYER_ID = "payerId";
    public static final String EXPERIENCE_PROFILE_ID = "experienceProfileId";

    public static final String LANGUAGE_CODE_FIELD = "languageCode";

    // payment by invoice
    public static final String REFERENCE = "reference";
    public static final String BANK_NAME = "paidToAccountBankName";
    public static final String ACCOUNT_HOLDER_NAME = "paidToAccountHolderName";
    public static final String IBAN = "paidToIBAN";
    public static final String BIC = "paidToBIC";
    public static final String PAYMENT_DUE_DATE = "paymentDueDate";
    public static final String AMOUNT = "amount";

    public static final String TIMESTAMP_FIELD = "timestamp";

    /**
     * <b>Note:</b> looks like for Paypal Plus in EU credit card token is never shared with payers, cos all the workflow
     * is executed in the iframe.
     */
    public static final String CREDIT_CARD_TOKEN = "cardDataPlaceholder";

    private CtpPaymentCustomFields() {
    }
}
