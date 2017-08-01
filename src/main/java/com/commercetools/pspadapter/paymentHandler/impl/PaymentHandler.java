package com.commercetools.pspadapter.paymentHandler.impl;

import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.model.CtpPaymentWithCart;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.facade.PaypalPlusFacade;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.payments.commands.updateactions.SetCustomField;
import io.sphere.sdk.payments.commands.updateactions.SetInterfaceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.commercetools.payment.constants.ctp.CtpPaymentCustomFields.APPROVAL_URL;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentInterfaceName.PAYPAL_PLUS;
import static com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse.of400BadRequest;
import static com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse.of500InternalServerError;
import static com.commercetools.pspadapter.tenant.TenantLoggerUtil.createLoggerName;
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

    private final Logger logger;

    public PaymentHandler(@Nonnull CtpFacade ctpFacade,
                          @Nonnull PaymentMapper paymentMapper,
                          @Nonnull PaypalPlusFacade paypalPlusFacade,
                          @Nonnull String tenantName) {
        this.ctpFacade = ctpFacade;
        this.paymentMapper = paymentMapper;
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
                                        .thenCompose(createdPpPayment -> updateCtpPayment(createdPpPayment, ctpPaymentId))
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

    public PaymentHandleResult executePayment(@Nonnull String paypalPlusPaymentId,
                                              @Nonnull String paypalPlusPayerId) {
        try {
            return paypalPlusFacade.getPaymentService().execute(new Payment().setId(paypalPlusPaymentId),
                    new PaymentExecution().setPayerId(paypalPlusPayerId))
                    .thenApply(payment -> new PaymentHandleResult(HttpStatus.OK,
                            payment.getTransactions().get(0).getRelatedResources().get(0).toJSON()))
                    // TODO: re-factor join!!!
                    .toCompletableFuture().join();
        } catch (Exception e) {
            logger.error("Error while processing payment ID {}", paypalPlusPaymentId, e);
            return new PaymentHandleResult(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected CompletionStage<Payment> updateCtpPayment(Payment newPpPayment, String ctpPaymentId) {
        List<UpdateAction<io.sphere.sdk.payments.Payment>> updateActions = asList(
                SetCustomField.ofObject(APPROVAL_URL, PaymentMapper.getApprovalUrl(newPpPayment).orElse("")),
                SetInterfaceId.of(newPpPayment.getId()));
        return ctpFacade.getPaymentService().updatePayment(ctpPaymentId, updateActions)
                .thenApply(ignore -> newPpPayment);
    }
}