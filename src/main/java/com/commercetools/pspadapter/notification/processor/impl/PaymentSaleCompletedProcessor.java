package com.commercetools.pspadapter.notification.processor.impl;

import com.commercetools.payment.constants.paypalPlus.NotificationEventType;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.google.gson.Gson;
import com.paypal.api.payments.Event;
import io.sphere.sdk.payments.Payment;
import io.sphere.sdk.payments.Transaction;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Processes event notification of type PAYMENT.SALE.COMPLETED
 */
@Component
public class PaymentSaleCompletedProcessor extends NotificationProcessorBase {

    @Autowired
    public PaymentSaleCompletedProcessor(Gson gson) {
        super(gson);
    }

    @Override
    public boolean canProcess(Event event) {
        return NotificationEventType.PAYMENT_SALE_COMPLETED.getPaypalEventTypeName()
                .equalsIgnoreCase(event.getEventType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<Optional<Payment>> getRelatedCtpPayment(CtpFacade ctpFacade, Event event) {
        Map<String, String> resource = (Map<String, String>) event.getResource();
        String ppPlusPaymentId = resource.get("parent_payment");

        return ctpFacade.getPaymentService()
                .getByPaymentInterfaceNameAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, ppPlusPaymentId);
    }

    @Override
    public ChangeTransactionState createUpdatePaymentStatus(Payment ctpPayment, Event event) {
        Optional<Transaction> txnOpt = findMatchingTxn(ctpPayment.getTransactions(), TransactionType.CHARGE, TransactionState.PENDING);
        return txnOpt.map(txn -> ChangeTransactionState.of(TransactionState.SUCCESS, txn.getId())).orElse(null);
    }
}