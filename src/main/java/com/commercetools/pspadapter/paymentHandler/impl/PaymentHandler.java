package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.exception.PaypalPlusException;
import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.helper.mapper.AddressMapper;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.PaymentMapperHelper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.payment.constants.ctp.CtpPaymentMethods;
import com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.paypal.api.payments.*;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.*;
import static com.commercetools.payment.constants.ctp.ExpansionExpressions.PAYMENT_INFO_EXPANSION;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPatchConstants.*;
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
    private final PaymentMapperHelper paymentMapperHelper;
    private final Gson gson;

    private final Logger logger;

    private final String PAYPAL_PLUS_PAYMENT_ID = "Paypal Plus payment ID";

    private final String CTP_PAYMENT_ID = "CTP payment ID";

    public PaymentHandler(@Nonnull CtpFacade ctpFacade,
                          @Nonnull PaymentMapperHelper paymentMapperHelper,
                          @Nonnull PaypalPlusFacade paypalPlusFacade,
                          @Nonnull String tenantName,
                          @Nonnull Gson gson) {
        this.ctpFacade = ctpFacade;
        this.paymentMapperHelper = paymentMapperHelper;
        this.paypalPlusFacade = paypalPlusFacade;
        this.gson = gson;
        this.logger = LoggerFactory.getLogger(createLoggerName(PaymentHandler.class, tenantName));
    }

    public CompletionStage<PaymentHandleResponse> createPayment(@Nonnull String ctpPaymentId) {
        try {
            CompletionStage<PaymentHandleResponse> createPaymentCS = ctpFacade.getPaymentService().getById(ctpPaymentId)
                    .thenCombineAsync(ctpFacade.getCartService().getByPaymentId(ctpPaymentId),
                            // TODO: re-factor this wasps nest!!!
                            (paymentOpt, cartOpt) -> {
                                if (!(paymentOpt.isPresent() && cartOpt.isPresent())) {
                                    return completedFuture(of404NotFound(
                                            format("Payment or cart for ctpPaymentId=[%s] not found", ctpPaymentId)));
                                }

                                io.sphere.sdk.payments.Payment ctpPayment = paymentOpt.get();

                                // TODO: andrii.kovalenko: this should be a common solution across all the controllers
                                // https://github.com/commercetools/commercetools-paypal-plus-integration/issues/38
                                if (!PAYPAL_PLUS.equals(ctpPayment.getPaymentMethodInfo().getPaymentInterface())) {
                                    return completedFuture(of400BadRequest(
                                            format("Payment ctpPaymentId=[%s] has incorrect payment interface: " +
                                                            "expected [%s], found [%s]", ctpPaymentId, PAYPAL_PLUS,
                                                    ctpPayment.getPaymentMethodInfo().getPaymentInterface())));
                                }

                                return createPaypalPlusPaymentAndUpdateCtpPayment(paymentOpt.get(), cartOpt.get())
                                        .thenApply(PaymentMapper::getApprovalUrl)
                                        .thenApply(approvalUrlOpt -> approvalUrlOpt
                                                .map(PaymentHandleResponse::of201CreatedApprovalUrl)
                                                .orElseGet(() -> of400BadRequest(
                                                        format("Payment or cart for ctpPaymentId=[%s] not found", ctpPaymentId))));
                            })
                    .thenCompose(stage -> stage); // flatten CompletionStage from createPaypalPlusPaymentAndUpdateCtpPayment()
            return runWithExceptionallyHandling(ctpPaymentId, CTP_PAYMENT_ID, createPaymentCS);
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", ctpPaymentId, e);
            return completedFuture(of500InternalServerError("See the logs"));
        }
    }

    public CompletionStage<PaymentHandleResponse> patchAddress(@Nonnull String ctpPaymentId) {
        return ctpFacade.getCartService().getByPaymentId(ctpPaymentId, PAYMENT_INFO_EXPANSION)
                .thenCombine(ctpFacade.getPaymentService().getById(ctpPaymentId),
                        (cartOpt, ctpPaymentOpt) -> {
                            if (cartOpt.isPresent() && ctpPaymentOpt.isPresent()
                                    && ctpPaymentOpt.get().getInterfaceId() != null) {
                                return patchAddress(cartOpt.get(), ctpPaymentOpt.get().getInterfaceId());
                            } else {
                                return completedFuture(PaymentHandleResponse.of400BadRequest(
                                        format("Payment payment=[%s] does not exists or does not have a connected cart.",
                                                ctpPaymentId)));
                            }
                        })
                .thenCompose(i -> i); // flatten CompletionStage from patchAddress()
    }

    /**
     * Patch address in default (non-installment) payment. This step should be committed by front-end after success
     * redirect before {@link #executePayment(String, String)}</b>
     * <p>
     * <b>Note:</b> this step is used only for {@link CtpPaymentMethods#DEFAULT} payment methods, that's why we use
     * {@code paymentMapperHelper.getDefaultPaymentMapper()} for address mapper defining.
     *
     * @param cartWithPaymentsExpansion cart from which patch the payment address
     * @param paypalPlusPaymentId       id of respective Paypal Plus payment to patch
     * @return Completion stage with success or error response.
     */
    public CompletionStage<PaymentHandleResponse> patchAddress(@Nonnull Cart cartWithPaymentsExpansion,
                                                               @Nonnull String paypalPlusPaymentId) {
        try {
            Optional<String> paymentIdOpt = getCtpPaymentId(cartWithPaymentsExpansion, paypalPlusPaymentId);
            return paymentIdOpt.map(paymentId -> {
                Payment paypalPlusPayment = new Payment().setId(paypalPlusPaymentId);
                List<Patch> patches = new ArrayList<>(2);

                io.sphere.sdk.models.Address shippingAddress = cartWithPaymentsExpansion.getShippingAddress();
                if (shippingAddress == null) {
                    return completedFuture(PaymentHandleResponse.of400BadRequest(format("Shipping address must not be null for cartId=[%s]", cartWithPaymentsExpansion.getId())));
                }

                // since patch is processed only for default payments - get default payment mapper
                final AddressMapper addressMapper = paymentMapperHelper.getDefaultPaymentMapper().getAddressMapper();

                Patch patchShippingAddress = new Patch(ADD_ACTION, SHIPPING_ADDRESS_PATH).setValue(addressMapper.ctpAddressToPaypalPlusShippingAddress(shippingAddress));
                patches.add(patchShippingAddress);

                // billing address is not mandatory
                io.sphere.sdk.models.Address billingAddress = cartWithPaymentsExpansion.getBillingAddress();
                if (billingAddress != null) {
                    Patch patchBillingAddress = new Patch(ADD_ACTION, PAYER_INFO_PATH)
                            .setValue(addressMapper.ctpAddressToPaypalPlusPayerInfo(billingAddress));
                    patches.add(patchBillingAddress);
                }
                AddInterfaceInteraction addInterfaceInteractionAction = createAddInterfaceInteractionAction(patchShippingAddress, REQUEST);
                CompletionStage<PaymentHandleResponse> patchCS = createPatchCompletionStage(paymentId, paypalPlusPayment, patches, addInterfaceInteractionAction);
                return runWithExceptionallyHandling(paypalPlusPaymentId, PAYPAL_PLUS_PAYMENT_ID, patchCS);
            })
                    .orElseGet(() -> completedFuture(PaymentHandleResponse.of404NotFound(format("Paypal Plus paymentId=[%s] cant be found on cartId=[%s]",
                            paypalPlusPaymentId, cartWithPaymentsExpansion.getId()))));
        } catch (Throwable e) {
            logger.error("Error while processing payment ID {}", paypalPlusPaymentId, e);
            return completedFuture(PaymentHandleResponse.of500InternalServerError(
                    format("Error while processing paymentId==[%s]", paypalPlusPaymentId)));
        }
    }

    public CompletionStage<PaymentHandleResponse> executePayment(@Nonnull String paypalPlusPaymentId,
                                                                 @Nonnull String paypalPlusPayerId) {
        CompletionStage<PaymentHandleResponse> executeCS = ctpFacade.getCartService()
                .getByPaymentMethodAndInterfaceId(PAYPAL_PLUS,
                        paypalPlusPaymentId, PAYMENT_INFO_EXPANSION)
                .thenCompose(cartOpt -> {
                    if (!cartOpt.isPresent()) {
                        return CompletableFuture.completedFuture(PaymentHandleResponse.of404NotFound(
                                format("Can't find cart with paymentId=[%s] and interfaceId=[%s]", paypalPlusPaymentId, PAYPAL_PLUS)));
                    } else {
                        return updatePayerIdInCtpPayment(paypalPlusPaymentId, paypalPlusPayerId)
                                .thenCompose(ctpPayment -> {
                                    if (ctpPayment == null) {
                                        return CompletableFuture.completedFuture(PaymentHandleResponse.of404NotFound(
                                                format("Payment not found for interfaceid=[%s]", paypalPlusPaymentId))
                                        );
                                    } else {
                                        return executePaymentAndCreateTxn(paypalPlusPaymentId, paypalPlusPayerId, ctpPayment);
                                    }
                                });
                    }
                });
        return runWithExceptionallyHandling(paypalPlusPaymentId, PAYPAL_PLUS_PAYMENT_ID, executeCS);
    }

    public CompletionStage<PaymentHandleResponse> lookUpPayment(String paypalPaymentId) {
        return runWithExceptionallyHandling(paypalPaymentId, PAYPAL_PLUS_PAYMENT_ID,
                paypalPlusFacade.getPaymentService().getByPaymentId(paypalPaymentId)
                        .thenApply(PaymentHandleResponse::of200OkResponseBody)
        );
    }

    protected CompletionStage<io.sphere.sdk.payments.Payment> updatePayerIdInCtpPayment(@Nonnull String paypalPlusPaymentId,
                                                                                        @Nonnull String payerId) {
        // TODO: lojzatran think about Optional, looks like it is proper place to use it here
        return ctpFacade.getPaymentService().getByPaymentInterfaceNameAndInterfaceId(PAYPAL_PLUS, paypalPlusPaymentId)
                .thenCompose(paymentOpt -> paymentOpt.map(payment -> {
                            List<UpdateAction<io.sphere.sdk.payments.Payment>> updateActions = Collections.singletonList(SetCustomField.ofObject(PAYER_ID, payerId));
                            return ctpFacade.getPaymentService().updatePayment(payment, updateActions);
                        }).orElse(completedFuture(null))
                );
    }

    protected List<UpdateAction<io.sphere.sdk.payments.Payment>> getApprovalUrlAndInterfaceIdAction(@Nonnull Payment paypalPayment) {
        return asList(
                SetCustomField.ofObject(APPROVAL_URL, PaymentMapper.getApprovalUrl(paypalPayment).orElse("")),
                SetInterfaceId.of(paypalPayment.getId()));
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

    protected CompletionStage<io.sphere.sdk.payments.Payment> createChargeTransaction(@Nonnull String paypalPlusPaymentId,
                                                                                      @Nonnull Payment paypalPayment,
                                                                                      @Nonnull io.sphere.sdk.payments.Payment ctpPayment) {
        if (PaypalPlusPaymentStates.APPROVED.equals(paypalPayment.getState())) {
            return createChargeTransaction(paypalPayment, ctpPayment.getId(), SUCCESS);
        } else if (PaypalPlusPaymentStates.CREATED.equals(paypalPayment.getState())
                || PaypalPlusPaymentStates.PENDING.equals(paypalPayment.getState())) {
            return createChargeTransaction(paypalPayment, ctpPayment.getId(), PENDING);
        } else {
            throw new PaypalPlusException(format("Error when approving payment paypalPlusPaymentId=[%s], current state=[%s]",
                    paypalPlusPaymentId, paypalPayment.getState()));
        }
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
                .thenCompose(updatedCtpPayment -> executeAndUpdatePayment(paypalPlusPaymentId, paymentExecution, updatedCtpPayment))
                .thenApply(ignore -> PaymentHandleResponse.ofHttpStatus(HttpStatus.CREATED));
    }

    private CompletionStage<CompletionStage<io.sphere.sdk.payments.Payment>> executeAndUpdatePayment(@Nonnull String paypalPlusPaymentId,
                                                                                                     @Nonnull PaymentExecution paymentExecution,
                                                                                                     @Nonnull io.sphere.sdk.payments.Payment payment) {
        return paypalPlusFacade.getPaymentService().execute(new Payment().setId(paypalPlusPaymentId),
                paymentExecution)
                .thenCompose(paypalPayment -> {

                    //  add paypal response to the ctp payment as interaction interface
                    return updateCtpPayment(paypalPayment, payment)
                            //  create charge transaction in the ctp payment
                            .thenApply(updatedCtpPayment2 -> createChargeTransaction(paypalPlusPaymentId, paypalPayment, updatedCtpPayment2));
                });

    }

    private CompletionStage<io.sphere.sdk.payments.Payment> updateCtpPayment(Payment paypalPayment, io.sphere.sdk.payments.Payment ctpPayment) {
        List<UpdateAction<io.sphere.sdk.payments.Payment>> actions = new ArrayList<>();
        actions.add(createAddInterfaceInteractionAction(paypalPayment, RESPONSE));
        PaymentInstruction paymentInstruction = paypalPayment.getPaymentInstruction();
        if (paymentInstruction != null) {
            actions.addAll(createAddPaymentInstructionAction(paymentInstruction));
        }
        return ctpFacade.getPaymentService().updatePayment(ctpPayment, actions);
    }

    private List<SetCustomField> createAddPaymentInstructionAction(PaymentInstruction paymentInstruction) {
        List<SetCustomField> setCustomFieldActions = new ArrayList<>(7);
        setCustomFieldActions.add(SetCustomField.ofObject(REFERENCE, paymentInstruction.getReferenceNumber()));
        setCustomFieldActions.add(SetCustomField.ofObject(BANK_NAME, paymentInstruction.getRecipientBankingInstruction().getBankName()));
        setCustomFieldActions.add(SetCustomField.ofObject(ACCOUNT_HOLDER_NAME, paymentInstruction.getRecipientBankingInstruction().getAccountHolderName()));
        setCustomFieldActions.add(SetCustomField.ofObject(IBAN, paymentInstruction.getRecipientBankingInstruction().getInternationalBankAccountNumber()));
        setCustomFieldActions.add(SetCustomField.ofObject(BIC, paymentInstruction.getRecipientBankingInstruction().getBankIdentifierCode()));
        setCustomFieldActions.add(SetCustomField.ofObject(PAYMENT_DUE_DATE, paymentInstruction.getPaymentDueDate()));
        setCustomFieldActions.add(SetCustomField.ofObject(AMOUNT, Money.of(new BigDecimal(paymentInstruction.getAmount().getValue()),
                paymentInstruction.getAmount().getCurrency())));
        return setCustomFieldActions;
    }

    private CompletionStage<PaymentHandleResponse> runWithExceptionallyHandling(@Nullable String paymentId,
                                                                                @Nullable String paymentIdType,
                                                                                @Nonnull CompletionStage<PaymentHandleResponse> completionStage) {

        return completionStage
                // handle() is used instead of exceptionally() to allow return CompletionStage from fallback saveResponseToInterfaceInteraction()
                .handle((response, throwable) -> {
                    if (throwable == null) {
                        return completedFuture(response);
                    }

                    logger.error("Unexpected exception processing " + paymentIdType + "=[{}]:", paymentId, throwable);
                    if (throwable instanceof CompletionException) {
                        // the real exception is wrapped inside
                        throwable = throwable.getCause();
                    }
                    if (throwable instanceof PaypalPlusServiceException) {
                        PayPalRESTException restException = ((PaypalPlusServiceException) throwable).getCause();
                        if (restException != null) {
                            return saveResponseToInterfaceInteraction(paymentId, paymentIdType, restException);
                        }
                    }
                    return completedFuture(PaymentHandleResponse.of400BadRequest(
                            format("%s [%s] can't be processed, details: [%s]", paymentIdType, paymentId, throwable.getMessage())));
                })
                .thenCompose(i -> i); // flatten CompletionStage from saveResponseToInterfaceInteraction
    }

    private CompletionStage<PaymentHandleResponse> saveResponseToInterfaceInteraction(@Nullable String paymentId,
                                                                                      @Nullable String paymentIdType,
                                                                                      @Nonnull PayPalRESTException restException) {
        CompletionStage<Optional<String>> ctpPaymentIdOptStage = CompletableFuture.completedFuture(Optional.ofNullable(paymentId));
        if (PAYPAL_PLUS_PAYMENT_ID.equals(paymentIdType)) {
            // if it's paypal plus payment id and not ctp payment id,
            // then fetch ctp payment to get ctp payment id
            ctpPaymentIdOptStage = this.ctpFacade.getPaymentService()
                    .getByPaymentInterfaceNameAndInterfaceId(PAYPAL_PLUS, paymentId)
                    .thenApply(ctpPayment -> ctpPayment.map(Resource::getId));
        }

        CompletionStage<PaymentHandleResponse> paymentHandleResponseStage = ctpPaymentIdOptStage
                .thenCompose(ctpPaymentIdOpt ->
                        ctpPaymentIdOpt.map(ctpPaymentId -> {
                            AddInterfaceInteraction action = createAddInterfaceInteractionAction(restException.getDetails(), RESPONSE);
                            return ctpFacade.getPaymentService().updatePayment(ctpPaymentId, Collections.singletonList(action))
                                    .thenApply(ignore -> PaymentHandleResponse.ofHttpStatusAndErrorMessage(HttpStatus.valueOf(restException.getResponsecode()),
                                            format("%s=[%s] can't be processed, details: [%s]", paymentIdType, paymentId, restException.getMessage())));
                        }).orElseGet(() -> CompletableFuture.completedFuture(
                                PaymentHandleResponse.of404NotFound(format("%s=[%s] is not found.", paymentIdType, paymentId))
                        ))
                );
        return paymentHandleResponseStage;
    }

    /**
     * <b>Note:</b> the paymentInfo must be expanded in the {@code cartWithPaymentsExpansion}, otherwise payment
     * won't be found.
     */
    private Optional<String> getCtpPaymentId(@Nonnull Cart cartWithPaymentsExpansion,
                                             @Nonnull String paypalPlusPaymentId) {
        return Optional.of(cartWithPaymentsExpansion)
                .map(Cart::getPaymentInfo)
                .map(PaymentInfo::getPayments)
                .flatMap(paymentReferences -> filterCtpPaymentByPaypalPlusPaymentId(paypalPlusPaymentId, paymentReferences))
                .map(Resource::getId);
    }

    private static Optional<io.sphere.sdk.payments.Payment> filterCtpPaymentByPaypalPlusPaymentId(@Nonnull String paypalPlusPaymentId,
                                                                                                  @Nonnull List<Reference<io.sphere.sdk.payments.Payment>> paymentReferences) {
        return paymentReferences.stream()
                .map(Reference::getObj)
                .filter(payment -> paypalPlusPaymentId.equals(payment.getInterfaceId()))
                .findFirst();
    }

    /**
     * Create payment on paypal plus,
     * saves approval URL, payment ID and interface interactions to CTP payment
     *
     * @return Paypal Plus payment
     */
    private CompletionStage<Payment> createPaypalPlusPaymentAndUpdateCtpPayment(@Nonnull io.sphere.sdk.payments.Payment ctpPayment,
                                                                                @Nonnull Cart ctpCart) {
        CtpPaymentWithCart paymentWithCart = new CtpPaymentWithCart(ctpPayment, ctpCart);

        PaymentMapper paymentMapper = paymentMapperHelper.getPaymentMapperOrDefault(paymentWithCart.getPaymentMethod());

        Payment paypalPlusPayment = paymentMapper.ctpPaymentToPaypalPlus(paymentWithCart);
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
                                                                              @Nonnull List<Patch> patches,
                                                                              @Nonnull AddInterfaceInteraction addInterfaceInteractionAction) {
        return ctpFacade.getPaymentService()
                // 1. add patch request to the ctp payment interface interaction
                .updatePayment(paymentId, Collections.singletonList(addInterfaceInteractionAction))
                .thenCompose(payment -> paypalPlusFacade.getPaymentService()
                        // 2. patch payment on paypal
                        .patch(paypalPlusPayment, patches)
                        .thenCompose(pPPayment -> {
                            List<UpdateAction<io.sphere.sdk.payments.Payment>> actionList
                                    = Collections.singletonList(createAddInterfaceInteractionAction(pPPayment, REQUEST));
                            // 3. save paypal response to the interface interaction in ctp payment
                            return ctpFacade.getPaymentService().updatePayment(payment, actionList);
                        })
                        .thenApply(ignore -> PaymentHandleResponse.ofHttpStatus(HttpStatus.OK)));
    }
}