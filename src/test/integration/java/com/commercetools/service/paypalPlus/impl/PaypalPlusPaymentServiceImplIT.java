package com.commercetools.service.paypalPlus.impl;

import com.commercetools.Application;
import com.commercetools.exception.IntegrationServiceException;
import com.commercetools.exception.PaypalPlusServiceException;
import com.commercetools.pspadapter.ExtendedAPIContextFactory;
import com.commercetools.service.paypalPlus.PaypalPlusPaymentService;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CompletionException;

import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentLinkRel.APPROVAL_URL;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.CREDIT_CARD;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentMethods.PAYPAL;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates.APPROVED;
import static com.commercetools.payment.constants.paypalPlus.PaypalPlusPaymentStates.CREATED;
import static com.commercetools.service.paypalPlus.PaypalPlusPaymentTestUtil.*;
import static com.commercetools.testUtil.CompletionStageUtil.executeBlocking;
import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static java.util.Optional.of;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
public class PaypalPlusPaymentServiceImplIT {

    @Autowired
    private PaypalPlusPaymentService paymentService;

    /**
     * It is used to supply store credit card.
     */
    @Autowired
    private ExtendedAPIContextFactory extendedApiContextFactory;

    @Test
    public void validatePaymentServiceContextInjection() {
        assertThat(paymentService).isExactlyInstanceOf(PaypalPlusPaymentServiceImpl.class);
    }

    @Test
    @Ignore("The payment method " + CREDIT_CARD + " is not used in Europe and false-fails often, thus ignored so far")
    public void createPseudoCreditCardPayment() throws Exception {
        CreditCard dummyCreditCard = dummyCreditCard();
        final CreditCard storedCreditCard = dummyCreditCard.create(extendedApiContextFactory.createAPIContext().getApiContext());

        Payment mockPayment = dummyCreditCardSecurePayment(storedCreditCard.getId());

        Payment savedPayment = executeBlocking(paymentService.create(mockPayment));

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotBlank();
        assertThat(savedPayment.getState()).isEqualTo(APPROVED);
        assertThat(savedPayment.getPayer().getPaymentMethod()).isEqualTo(CREDIT_CARD);
        List<FundingInstrument> savedFundingInstruments = of(savedPayment).map(Payment::getPayer)
                .map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedFundingInstruments).isNotNull();
        assertThat(savedFundingInstruments.size()).isEqualTo(1);
        FundingInstrument savedFundingInstrument = savedFundingInstruments.get(0);
        assertThat(savedFundingInstrument.getCreditCardToken().getCreditCardId()).isEqualTo(storedCreditCard.getId());
        assertThat(savedFundingInstrument.getCreditCardToken().getLast4()).isEqualTo("0779");
    }


    @Test
    @Ignore("The payment method " + CREDIT_CARD + " is not used in Europe and false-fails often, thus ignored so far")
    public void createCreditCardPayment() throws Exception {
        Payment mockPayment = dummyCreditCardSimplePayment();

        Payment savedPayment = executeBlocking(paymentService.create(mockPayment));

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotBlank();
        assertThat(savedPayment.getState()).isEqualTo(APPROVED);
        assertThat(savedPayment.getPayer().getPaymentMethod()).isEqualTo(CREDIT_CARD);
        List<FundingInstrument> savedList = of(savedPayment).map(Payment::getPayer).map(Payer::getFundingInstruments).orElse(null);
        assertThat(savedList).isNotNull();
        assertThat(savedList.size()).isEqualTo(1);
        assertThat(savedList.get(0).getCreditCard().getNumber()).isEqualTo("xxxxxxxxxxxx0779");
    }

    @Test
    public void createPaypalPayment() throws Exception {
        Payment dummyPayment = dummyPaypalPayment();

        Payment savedPayment = executeBlocking(paymentService.create(dummyPayment));

        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getId()).isNotBlank();
        assertThat(savedPayment.getState()).isEqualTo(CREATED);
        assertThat(savedPayment.getPayer().getPaymentMethod()).isEqualTo(PAYPAL);

        Item soldItem = savedPayment.getTransactions().stream()
                .map(tr -> tr.getItemList().getItems().stream().findFirst().orElse(null))
                .findFirst().orElse(null);
        assertThat(soldItem).isNotNull();
        assertThat(soldItem.getName()).isEqualTo("Ground Coffee 40 oz");
        assertThat(soldItem.getPrice()).isEqualTo("5.33");

        // verify redirect URL
        String approvalUrl = savedPayment.getLinks().stream()
                .filter(link -> APPROVAL_URL.equals(link.getRel()))
                .map(Links::getHref)
                .findFirst()
                .orElse("");

        assertThat(approvalUrl).startsWith("https://www.sandbox.paypal.com/");
    }

    @Test
    public void createPaypalPaymentWithIntegrationServiceException() throws Exception {
        Payment savedPayment = null;
        try {
            Payment dummyPayment = Mockito.mock(Payment.class);
            Mockito.when(dummyPayment.create(Mockito.any(APIContext.class))).thenThrow(new RuntimeException("Unknown exception"));
            savedPayment = executeBlocking(paymentService.create(dummyPayment));
        } catch (CompletionException ex) {
            IntegrationServiceException e = (IntegrationServiceException) ex.getCause();
            assertThat(e.getMessage()).isNotEmpty();
            assertTrue(e.getMessage().contains(MAIN_TEST_TENANT_NAME));
        }
    }

    @Test
    public void createPaypalPaymentWithPayPalRESTException() throws Exception {
        Payment savedPayment = null;
        try {
            Payment dummyPayment = Mockito.mock(Payment.class);
            Mockito.when(dummyPayment.create(Mockito.any(APIContext.class))).thenThrow(new PayPalRESTException("known paypal exception"));
            savedPayment = executeBlocking(paymentService.create(dummyPayment));
        } catch (CompletionException ex) {
            PaypalPlusServiceException e = (PaypalPlusServiceException) ex.getCause();
            assertThat(e.getMessage()).isNotEmpty();
            assertTrue(e.getMessage().contains(MAIN_TEST_TENANT_NAME));
        }
    }

}
