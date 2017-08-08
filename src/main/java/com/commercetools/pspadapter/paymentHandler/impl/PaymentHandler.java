package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.exception.PaypalPlusPaymentException;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.ShippingAddressMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.ShippingAddress;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.TransactionDraft;
import io.sphere.sdk.payments.TransactionDraftBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import io.sphere.sdk.payments.commands.updateactions.SetCustomField;
import io.sphere.sdk.payments.commands.updateactions.SetInterfaceId;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.PAYER_ID;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse.of400BadRequest;
import static com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse.of500InternalServerError;
import static com.commercetools.pspadapter.tenant.TenantLoggerUtil.createLoggerName;
import static com.commercetools.util.TimeUtil.toZonedDateTime;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Handles all actions related to the payment. This class is created
 * tenant-specific and user does not need to provide tenants for every action.
 */
public class PaymentHandler {

    private final CtpFacade ctpFacade;
    private final PaypalPlusFacade paypalPlusFacade;
    private final PaymentMapper paymentMapper;
    private final ShippingAddressMapper shippingAddressMapper;

    private final Logger logger;

    public PaymentHandler(@Nonnull CtpFacade ctpFacade,
                          @Nonnull PaymentMapper paymentMapper,
                          @Nonnull ShippingAddressMapper shippingAddressMapper,
                          @Nonnull PaypalPlusFacade paypalPlusFacade,
                          @Nonnull String tenantName) {
        this.ctpFacade = ctpFacade;
        this.paymentMapper = paymentMapper;
        this.shippingAddressMapper = shippingAddressMapper;
        this.paypalPlusFacade = paypalPlusFacade;
        this.logger = LoggerFactory.getLogger(createLoggerName(PaymentHandler.class, tenantName));
    }

    public PaymentHandleResponse createPayment(@Nonnull String ctpPaymentId) {
        try {
            return ctpFacade.getPaymentService().getById(ctpPaymentId)
                    .thenCombineAsync(ctpFacade.getCartService().getByPaymentId(ctpPaymentId),
                            // TODO: re-factor this wasps nest!!!
                            (optPayment, optCart) -> {
                                if (!(optPayment.isPresent() && optCart.isPresent())) {
                                    return completedFuture(of400BadRequest(
                                            format("Payment or cart for ctpPaymentId=[%s] not found", ctpPaymentId)));
                                }

                                io.sphere.sdk.payments.Payment ctpPayment = optPayment.get();

                                // TODO: andrii.kovalenko: this should be a common solution across all the controllers
                                if (!PAYPAL_PLUS.equals(ctpPayment.getPaymentMethodInfo().getPaymentInterface())) {
                                    completedFuture(of400BadRequest(
                                            format("Payment ctpPaymentId=[%s] has incorrect payment interface: " +
                                                            "expected [%s], found [%s]", ctpPaymentId, PAYPAL_PLUS,
                                                    ctpPayment.getPaymentMethodInfo().getPaymentInterface())));
                                }

                                Payment paypalPlusPayment = paymentMapper.ctpPaymentToPaypalPlus(
                                        new CtpPaymentWithCart(optPayment.get(), optCart.get()));

                                return paypalPlusFacade.getPaymentService().create(paypalPlusPayment)
                                        .thenCompose(createdPpPayment -> setApprovalUrlAndInterfaceId(createdPpPayment, ctpPaymentId))
                                        .thenApply(PaymentMapper::getApprovalUrl)
                                        .thenApply(approvalUrlOpt -> approvalUrlOpt
                                                .map(PaymentHandleResponse::of201CreatedApprovalUrl)
                                                .orElseGet(() -> of400BadRequest(
                                                        format("Payment or cart for ctpPaymentId=[%s] not found", ctpPaymentId))));
                            })
                    // TODO: re-factor compose !!!!
                    .thenCompose(stage -> stage)
                    .exceptionally(throwable -> {
                        logger.error("Unexpected exception handling payment [{}]:", ctpPaymentId, throwable);
                        return of400BadRequest(
                                format("Payment or cart for ctpPaymentId=[%s] can't be processed, see the logs", ctpPaymentId));
                    })
                    // TODO: re-factor join !!!!
                    .toCompletableFuture().join();
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", ctpPaymentId, e);
            return of500InternalServerError("See the logs");
        }
    }

    public PaymentHandleResponse patchAddress(@Nonnull Cart cart, @Nonnull String paypalPlusPaymentId) {
        try {
            Payment paypalPlusPayment = new Payment().setId(paypalPlusPaymentId);
            ShippingAddress shippingAddress = shippingAddressMapper.ctpAddressToPaypalPlusAddress(cart);
            Patch replace = new Patch("add", "/transactions/0/item_list/shipping_address").setValue(shippingAddress);
            CompletionStage<PaymentHandleResponse> patchCS = paypalPlusFacade.getPaymentService().patch(paypalPlusPayment, replace)
                    .thenApply(payment -> PaymentHandleResponse.ofStatusCode(HttpStatus.OK));
            return executeWithExceptionallyHandling(paypalPlusPaymentId, patchCS);
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", paypalPlusPaymentId, e);
            return PaymentHandleResponse.of500InternalServerError(
                    format("Error while processing paymentId==[%s]", paypalPlusPaymentId));
        }
    }

    public PaymentHandleResponse executePayment(@Nonnull String paypalPlusPaymentId,
                                                @Nonnull String paypalPlusPayerId) {
        CompletionStage<PaymentHandleResponse> executeCS = ctpFacade.getCartService()
                .getByPaymentMethodAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS, paypalPlusPaymentId)
                .thenCompose(cartOpt -> {
                    if (!cartOpt.isPresent()) {
                        return CompletableFuture.completedFuture(PaymentHandleResponse.of404NotFound(
                                format("Can't find cart with interfaceId==[%s]", paypalPlusPaymentId)));
                    } else {
                        Cart cart = cartOpt.get();
                        PaymentHandleResponse paymentHandleResponse = patchAddress(cart, paypalPlusPaymentId);
                        boolean isSuccessful = HttpStatus.valueOf(paymentHandleResponse.getStatusCode()).is2xxSuccessful();
                        if (isSuccessful) {
                            // execute payment only when patching was successful
                            return updatePayerIdInCtpPayment(paypalPlusPaymentId, paypalPlusPayerId)
                                    .thenCompose(ctpPayment -> executePaymentAndCreateTxn(paypalPlusPaymentId, paypalPlusPayerId, ctpPayment));
                        } else {
                            return CompletableFuture.completedFuture(paymentHandleResponse);
                        }
                    }
                });
        return executeWithExceptionallyHandling(paypalPlusPaymentId, executeCS);
    }

    protected CompletionStage<io.sphere.sdk.payments.Payment> updatePayerIdInCtpPayment(String paypalPlusPaymentId, String payerId) {
        return ctpFacade.getPaymentService().getByPaymentMethodAndInterfaceId(PAYPAL_PLUS, paypalPlusPaymentId)
                .thenCompose((paymentOpt) -> paymentOpt.map(payment -> {
                    List<UpdateAction<io.sphere.sdk.payments.Payment>> updateActions = Collections.singletonList(SetCustomField.ofObject(PAYER_ID, payerId));
                    return ctpFacade.getPaymentService().updatePayment(payment, updateActions);
                }).orElse(null));
    }

    protected CompletionStage<io.sphere.sdk.payments.Payment> createChargeTransaction(Payment paypalPayment, String ctpPaymentId, TransactionState transactionState) {
        Amount totalAmount = paypalPayment.getTransactions().get(0).getAmount();
        BigDecimal total = new BigDecimal(totalAmount.getTotal());
        String updateTimeStr = paypalPayment.getUpdateTime() == null ? paypalPayment.getCreateTime() : paypalPayment.getUpdateTime();
        TransactionDraft transactionDraft = TransactionDraftBuilder
                .of(TransactionType.CHARGE, Money.of(total, totalAmount.getCurrency()))
                .timestamp(toZonedDateTime(updateTimeStr))
                .state(transactionState)
                .build();
        return ctpFacade.getPaymentService()
                .updatePayment(ctpPaymentId, Collections.singletonList(AddTransaction.of(transactionDraft)));
    }

    protected CompletionStage<Payment> setApprovalUrlAndInterfaceId(Payment newPpPayment, String ctpPaymentId) {
        List<UpdateAction<io.sphere.sdk.payments.Payment>> updateActions = asList(
                SetCustomField.ofObject(APPROVAL_URL, PaymentMapper.getApprovalUrl(newPpPayment).orElse("")),
                SetInterfaceId.of(newPpPayment.getId()));
        return ctpFacade.getPaymentService().updatePayment(ctpPaymentId, updateActions)
                .thenApply(ignore -> newPpPayment);
    }

    private CompletionStage<PaymentHandleResponse> executePaymentAndCreateTxn(@Nonnull String paypalPlusPaymentId,
                                                                              @Nonnull String paypalPlusPayerId,
                                                                              @Nonnull io.sphere.sdk.payments.Payment ctpPayment) {
        return paypalPlusFacade.getPaymentService().execute(new Payment().setId(paypalPlusPaymentId),
                new PaymentExecution().setPayerId(paypalPlusPayerId))
                .thenCompose(paypalPayment -> {
                    if (PaypalPlusPaymentStates.APPROVED.equals(paypalPayment.getState())) {
                        return createChargeTransaction(paypalPayment, ctpPayment.getId(), TransactionState.SUCCESS);
                    } else if (PaypalPlusPaymentStates.CREATED.equals(paypalPayment.getState())) {
                        return createChargeTransaction(paypalPayment, ctpPayment.getId(), TransactionState.PENDING);
                    } else {
                        throw new PaypalPlusPaymentException(format("Error when approving payment [%s], current state=[%s]",
                                paypalPlusPaymentId, paypalPayment.getState()));
                    }
                })
                .thenApply(payment -> PaymentHandleResponse.ofStatusCode(HttpStatus.OK));
    }

    private PaymentHandleResponse executeWithExceptionallyHandling(String paypalPlusPaymentId,
                                                                   CompletionStage<PaymentHandleResponse> completionStage) {
        return completionStage
                .exceptionally(throwable -> {
                    logger.error("Unexpected exception processing Paypal Plus paymentId=[{}]:", paypalPlusPaymentId, throwable);
                    return PaymentHandleResponse.of400BadRequest(
                            format("PaypalPlus payment [%s] can't be processed, see the logs", paypalPlusPaymentId));
                }).toCompletableFuture().join();

    }
}