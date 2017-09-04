package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.exception.MissingExpansionException;
import com.commercetools.exception.PaypalPlusException;
import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.ShippingAddressMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Patch;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.ShippingAddress;
import com.paypal.base.rest.PayPalModel;
import com.paypal.base.rest.PayPalRESTException;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.carts.PaymentInfo;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.models.Resource;
import io.sphere.sdk.payments.TransactionDraft;
import io.sphere.sdk.payments.TransactionDraftBuilder;
import io.sphere.sdk.payments.TransactionState;
import io.sphere.sdk.payments.TransactionType;
import io.sphere.sdk.payments.commands.updateactions.AddInterfaceInteraction;
import io.sphere.sdk.payments.commands.updateactions.AddTransaction;
import io.sphere.sdk.payments.commands.updateactions.SetCustomField;
import io.sphere.sdk.payments.commands.updateactions.SetInterfaceId;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.ctp.ExpansionExpressions.PAYMENT_INFO_EXPANSION;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType.REQUEST;
import static com.commercetools.pspadapter.paymentHandler.impl.InterfaceInteractionType.RESPONSE;
import static com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse.*;
import static com.commercetools.pspadapter.tenant.TenantLoggerUtil.createLoggerName;
import static com.commercetools.util.TimeUtil.toZonedDateTime;
import static io.sphere.sdk.payments.TransactionState.PENDING;
import static io.sphere.sdk.payments.TransactionState.SUCCESS;
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
    private final Gson gson;

    private final Logger logger;

    private final String PAYPAL_PLUS_PAYMENT_ID = "Paypal Plus payment ID";

    private final String CTP_PAYMENT_ID = "CTP payment ID";


    public PaymentHandler(@Nonnull CtpFacade ctpFacade,
                          @Nonnull PaymentMapper paymentMapper,
                          @Nonnull ShippingAddressMapper shippingAddressMapper,
                          @Nonnull PaypalPlusFacade paypalPlusFacade,
                          @Nonnull String tenantName,
                          @Nonnull Gson gson) {
        this.ctpFacade = ctpFacade;
        this.paymentMapper = paymentMapper;
        this.shippingAddressMapper = shippingAddressMapper;
        this.paypalPlusFacade = paypalPlusFacade;
        this.gson = gson;
        this.logger = LoggerFactory.getLogger(createLoggerName(PaymentHandler.class, tenantName));
    }

    public PaymentHandleResponse createPayment(@Nonnull String ctpPaymentId) {
        try {
            CompletionStage<PaymentHandleResponse> createPaymentCS = ctpFacade.getPaymentService().getById(ctpPaymentId)
                    .thenCombineAsync(ctpFacade.getCartService().getByPaymentId(ctpPaymentId),
                            // TODO: re-factor this wasps nest!!!
                            (optPayment, optCart) -> {
                                if (!(optPayment.isPresent() && optCart.isPresent())) {
                                    return completedFuture(of404NotFound(
                                            format("Payment or cart for ctpPaymentId=[%s] not found", ctpPaymentId)));
                                }

                                io.sphere.sdk.payments.Payment ctpPayment = optPayment.get();

                                // TODO: andrii.kovalenko: this should be a common solution across all the controllers
                                // https://github.com/commercetools/commercetools-paypal-plus-integration/issues/38
                                if (!PAYPAL_PLUS.equals(ctpPayment.getPaymentMethodInfo().getPaymentInterface())) {
                                    return completedFuture(of400BadRequest(
                                            format("Payment ctpPaymentId=[%s] has incorrect payment interface: " +
                                                            "expected [%s], found [%s]", ctpPaymentId, PAYPAL_PLUS,
                                                    ctpPayment.getPaymentMethodInfo().getPaymentInterface())));
                                }

                                return createPaypalPlusPaymentAndUpdateCtpPayment(optPayment.get(), optCart.get())
                                        .thenApply(PaymentMapper::getApprovalUrl)
                                        .thenApply(approvalUrlOpt -> approvalUrlOpt
                                                .map(PaymentHandleResponse::of201CreatedApprovalUrl)
                                                .orElseGet(() -> of400BadRequest(
                                                        format("Payment or cart for ctpPaymentId=[%s] not found", ctpPaymentId))));
                            })
                    // TODO: re-factor compose !!!!
                    .thenCompose(stage -> stage);
            return runWithExceptionallyHandling(ctpPaymentId, CTP_PAYMENT_ID, createPaymentCS);
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", ctpPaymentId, e);
            return of500InternalServerError("See the logs");
        }
    }

    public PaymentHandleResponse patchAddress(@Nonnull Cart cartWithPaymentsExpansion,
                                              @Nonnull String paypalPlusPaymentId) {
        try {
            Optional<String> paymentIdOpt = getCtpPaymentId(cartWithPaymentsExpansion, paypalPlusPaymentId);
            return paymentIdOpt.map(paymentId -> {
                Payment paypalPlusPayment = new Payment().setId(paypalPlusPaymentId);
                ShippingAddress shippingAddress = shippingAddressMapper.ctpAddressToPaypalPlusAddress(cartWithPaymentsExpansion.getShippingAddress());
                Patch replace = new Patch("add", "/transactions/0/item_list/shipping_address").setValue(shippingAddress);
                AddInterfaceInteraction addInterfaceInteractionAction = createAddInterfaceInteractionAction(replace, REQUEST);
                CompletionStage<PaymentHandleResponse> patchCS = createPatchCompletionStage(paymentId, paypalPlusPayment, replace, addInterfaceInteractionAction);
                return runWithExceptionallyHandling(paypalPlusPaymentId, PAYPAL_PLUS_PAYMENT_ID, patchCS);
            }).orElseGet(() -> PaymentHandleResponse.of404NotFound(format("Paypal Plus paymentId=[%s] cant be found on cartId=[%s]",
                    paypalPlusPaymentId, cartWithPaymentsExpansion.getId())));
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", paypalPlusPaymentId, e);
            return PaymentHandleResponse.of500InternalServerError(
                    format("Error while processing paymentId==[%s]", paypalPlusPaymentId));
        }
    }

    public PaymentHandleResponse executePayment(@Nonnull String paypalPlusPaymentId,
                                                @Nonnull String paypalPlusPayerId) {
        CompletionStage<PaymentHandleResponse> executeCS = ctpFacade.getCartService()
                .getByPaymentMethodAndInterfaceId(PaypalPlusPaymentInterfaceName.PAYPAL_PLUS,
                        paypalPlusPaymentId, PAYMENT_INFO_EXPANSION)
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
        return runWithExceptionallyHandling(paypalPlusPaymentId, PAYPAL_PLUS_PAYMENT_ID, executeCS);
    }

    protected CompletionStage<io.sphere.sdk.payments.Payment> updatePayerIdInCtpPayment(@Nullable String paypalPlusPaymentId,
                                                                                        @Nonnull String payerId) {
        return ctpFacade.getPaymentService().getByPaymentMethodAndInterfaceId(PAYPAL_PLUS, paypalPlusPaymentId)
                .thenCompose((paymentOpt) -> paymentOpt.map(payment -> {
                    List<UpdateAction<io.sphere.sdk.payments.Payment>> updateActions = Collections.singletonList(SetCustomField.ofObject(PAYER_ID, payerId));
                    return ctpFacade.getPaymentService().updatePayment(payment, updateActions);
                }).orElse(null));
    }

    protected CompletionStage<io.sphere.sdk.payments.Payment> createChargeTransaction(@Nonnull Payment paypalPayment,
                                                                                      @Nonnull String ctpPaymentId,
                                                                                      @Nullable TransactionState transactionState) {
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

    protected List<UpdateAction<io.sphere.sdk.payments.Payment>> getApprovalUrlAndInterfaceIdAction(@Nonnull Payment paypalPayment) {
        return asList(
                SetCustomField.ofObject(APPROVAL_URL, PaymentMapper.getApprovalUrl(paypalPayment).orElse("")),
                SetInterfaceId.of(paypalPayment.getId()));
    }

    /**
     * Private methods
     **/

    private AddInterfaceInteraction createAddInterfaceInteractionAction(@Nonnull PayPalModel model,
                                                                        @Nonnull InterfaceInteractionType type) {
        String json = this.gson.toJson(model);
        return AddInterfaceInteraction.ofTypeKeyAndObjects(type.getInterfaceKey(),
                ImmutableMap.of(type.getValueFieldName(), json,
                        TIMESTAMP_FIELD, ZonedDateTime.now()));
    }

    private CompletionStage<PaymentHandleResponse> executePaymentAndCreateTxn(@Nonnull String paypalPlusPaymentId,
                                                                              @Nonnull String paypalPlusPayerId,
                                                                              @Nonnull io.sphere.sdk.payments.Payment ctpPayment) {
        PaymentExecution paymentExecution = new PaymentExecution().setPayerId(paypalPlusPayerId);

        AddInterfaceInteraction interactionAction = createAddInterfaceInteractionAction(paymentExecution, REQUEST);
        return ctpFacade.getPaymentService()
                .updatePayment(ctpPayment.getId(), Collections.singletonList(interactionAction))
                .thenCompose(payment -> executeAndUpdatePayment(paypalPlusPaymentId, paymentExecution, payment))
                .thenApply(ignore -> PaymentHandleResponse.ofHttpStatus(HttpStatus.CREATED));
    }

    private CompletionStage<CompletionStage<io.sphere.sdk.payments.Payment>> executeAndUpdatePayment(@Nonnull String paypalPlusPaymentId,
                                                                                                     @Nonnull PaymentExecution paymentExecution,
                                                                                                     @Nonnull io.sphere.sdk.payments.Payment payment) {
        return paypalPlusFacade.getPaymentService().execute(new Payment().setId(paypalPlusPaymentId), paymentExecution)
                .thenCompose(paypalPayment -> {
                    AddInterfaceInteraction addInteractionAction = createAddInterfaceInteractionAction(paypalPayment, RESPONSE);
                    // add paypal response to the ctp payment as interaction interface
                    return ctpFacade.getPaymentService().updatePayment(payment, Collections.singletonList(addInteractionAction))
                            // create charge transaction in the ctp payment
                            .thenApply(updatedCtpPayment -> createChargeTransaction(paypalPlusPaymentId, paypalPayment, updatedCtpPayment));
                });
    }

    private CompletionStage<io.sphere.sdk.payments.Payment> createChargeTransaction(@Nonnull String paypalPlusPaymentId,
                                                                                    @Nonnull Payment paypalPayment,
                                                                                    @Nonnull io.sphere.sdk.payments.Payment ctpPayment) {
        if (PaypalPlusPaymentStates.APPROVED.equals(paypalPayment.getState())) {
            return createChargeTransaction(paypalPayment, ctpPayment.getId(), SUCCESS);
        } else if (PaypalPlusPaymentStates.CREATED.equals(paypalPayment.getState())) {
            return createChargeTransaction(paypalPayment, ctpPayment.getId(), PENDING);
        } else {
            throw new PaypalPlusException(format("Error when approving payment paypalPlusPaymentId=[%s], current state=[%s]",
                    paypalPlusPaymentId, paypalPayment.getState()));
        }
    }

    private PaymentHandleResponse runWithExceptionallyHandling(@Nullable String paymentId,
                                                               @Nullable String paymentIdType,
                                                               @Nonnull CompletionStage<PaymentHandleResponse> completionStage) {
        CompletionStage<PaymentHandleResponse> exceptionally = completionStage
                .exceptionally(throwable -> {
                    logger.error("Unexpected exception processing " + paymentIdType + "=[{}]:", paymentId, throwable);
                    if (throwable instanceof CompletionException) {
                        // the real exception is wrapped inside
                        throwable = throwable.getCause();
                    }
                    if (throwable instanceof PaypalPlusServiceException) {
                        PayPalRESTException restException = ((PaypalPlusServiceException) throwable).getCause();
                        if (restException != null) {
                            return saveResponseToInterfaceInteraction(paymentId, paymentIdType, restException)
                                    .toCompletableFuture().join();
                        }
                    }
                    CompletionStage<PaymentHandleResponse> paymentHandleResponseStage = CompletableFuture.completedFuture(PaymentHandleResponse.of400BadRequest(
                            format("%s [%s] can't be processed, details: [%s]", paymentIdType, paymentId, throwable.getMessage())));
                    return paymentHandleResponseStage.toCompletableFuture().join();
                });
        return exceptionally.toCompletableFuture().join();
    }

    private CompletionStage<PaymentHandleResponse> saveResponseToInterfaceInteraction(@Nullable String paymentId,
                                                                                      @Nullable String paymentIdType,
                                                                                      @Nonnull PayPalRESTException restException) {
        CompletionStage<String> ctpPaymentIdStage = CompletableFuture.completedFuture(paymentId);
        if (PAYPAL_PLUS_PAYMENT_ID.equals(paymentIdType)) {
            // if it's paypal plus payment id and not ctp payment id,
            // then fetch ctp payment to get ctp payment id
            ctpPaymentIdStage = this.ctpFacade.getPaymentService()
                    .getByPaymentMethodAndInterfaceId(PAYPAL_PLUS, paymentId)
                    .thenApply(ctpPayment -> ctpPayment.map(Resource::getId).orElse(null));
        }

        CompletionStage<PaymentHandleResponse> paymentHandleResponseStage = ctpPaymentIdStage
                .thenCompose(ctpPaymentId -> {
                    AddInterfaceInteraction action = createAddInterfaceInteractionAction(restException.getDetails(), RESPONSE);
                    return ctpFacade.getPaymentService().updatePayment(ctpPaymentId, Collections.singletonList(action))
                            .thenApply(ignore -> PaymentHandleResponse.ofHttpStatusAndErrorMessage(HttpStatus.valueOf(restException.getResponsecode()),
                                    format("%s=[%s] can't be processed, details: [%s]", paymentIdType, paymentId, restException.getMessage())));
                });
        return paymentHandleResponseStage;
    }

    private Optional<String> getCtpPaymentId(@Nonnull Cart cartWithPaymentsExpansion,
                                             @Nonnull String paypalPlusPaymentId) {
        return Optional.of(cartWithPaymentsExpansion)
                .map(Cart::getPaymentInfo)
                .map(PaymentInfo::getPayments)
                .map(Collection::stream)
                .map(paymentsStream -> filterCtpPaymentIdByPaypalPlusPaymentId(paypalPlusPaymentId, paymentsStream))
                .orElseThrow(() -> {
                    throw new MissingExpansionException(format("Please expand Cart with cart.%s for cartId=[%s]",
                            PAYMENT_INFO_EXPANSION, cartWithPaymentsExpansion.getId()));
                });
    }

    private Optional<String> filterCtpPaymentIdByPaypalPlusPaymentId(@Nonnull String paypalPlusPaymentId,
                                                                     @Nonnull Stream<Reference<io.sphere.sdk.payments.Payment>> referenceStream) {
        return referenceStream
                .map(Reference::getObj)
                .filter(payment -> paypalPlusPaymentId.equals(payment.getInterfaceId()))
                .findAny()
                .map(Resource::getId);
    }

    /**
     * Create payment on paypal plus,
     * saves approval URL, payment ID and interface interactions to CTP payment
     *
     * @return Paypal Plus payment
     */
    private CompletionStage<Payment> createPaypalPlusPaymentAndUpdateCtpPayment(@Nonnull io.sphere.sdk.payments.Payment ctpPayment,
                                                                                @Nonnull Cart ctpCart) {
        Payment paypalPlusPayment = paymentMapper.ctpPaymentToPaypalPlus(
                new CtpPaymentWithCart(ctpPayment, ctpCart));
        AddInterfaceInteraction action = createAddInterfaceInteractionAction(paypalPlusPayment, REQUEST);
        // 1. save create paypal request to ctp payment's interface interaction
        return ctpFacade.getPaymentService().updatePayment(ctpPayment, Collections.singletonList(action))
                .thenCompose(payment -> {
                    // 2. create paypal payment
                    return paypalPlusFacade.getPaymentService().create(paypalPlusPayment)
                            .thenCompose(createdPpPayment -> {
                                        List<UpdateAction<io.sphere.sdk.payments.Payment>> updateActions
                                                = new ArrayList<>(getApprovalUrlAndInterfaceIdAction(createdPpPayment));
                                        AddInterfaceInteraction interaction = createAddInterfaceInteractionAction(createdPpPayment, RESPONSE);
                                        updateActions.add(interaction);
                                        // 3. save approval url and payment ID and response from paypal to ctp payment
                                        return ctpFacade.getPaymentService().updatePayment(payment, updateActions)
                                                .thenApply(ignore -> createdPpPayment);
                                    }
                            );
                });
    }


    private CompletionStage<PaymentHandleResponse> createPatchCompletionStage(@Nonnull String paymentId,
                                                                              @Nonnull Payment paypalPlusPayment,
                                                                              @Nonnull Patch replace,
                                                                              @Nonnull AddInterfaceInteraction addInterfaceInteractionAction) {
        return ctpFacade.getPaymentService()
                // 1. add patch request to the ctp payment interface interaction
                .updatePayment(paymentId, Collections.singletonList(addInterfaceInteractionAction))
                .thenCompose(payment -> paypalPlusFacade.getPaymentService()
                        // 2. patch payment on paypal
                        .patch(paypalPlusPayment, replace)
                        .thenCompose(pPPayment -> {
                            List<UpdateAction<io.sphere.sdk.payments.Payment>> actionList
                                    = Collections.singletonList(createAddInterfaceInteractionAction(pPPayment, REQUEST));
                            // 3. save paypal response to the interface interaction in ctp payment
                            return ctpFacade.getPaymentService().updatePayment(payment, actionList);
                        })
                        .thenApply(ignore -> PaymentHandleResponse.ofHttpStatus(HttpStatus.OK)));
    }
}