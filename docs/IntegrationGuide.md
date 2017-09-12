# commercetools _Paypal Plus_ Service Integration Guide

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [commercetools _Paypal Plus_ Service Integration Guide](#commercetools-_paypal-plus_-service-integration-guide)
  - [Setup docker container](#setup-docker-container)
  - [Preparing Paypal Plus accounts](#preparing-paypal-plus-accounts)
  - [Preparing commercetools Platform accounts](#preparing-commercetools-platform-accounts)
  - [Front-end workflow](#front-end-workflow)
  - [Create default payments](#create-default-payments)
    - [Create installment (rate) payments](#create-installment-rate-payments)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

This documentation describes how to setup, start and use `commercetools-paypal-plus-integtration-java` service.

## Setup docker container

## Preparing Paypal Plus accounts

## Preparing commercetools Platform accounts

## Front-end workflow

## Create default payments

Defaults payments are _Credit Card_, _Paypal_, _Invoice_.

### Create installment (rate) payments

Installment payment type requires different workflow because of especial security requirements from Paypal Plus.

  1. At the payment selection page the Installments Button only has to be displayed for Cart values above 99,00€.
  
  2. CTP ([PaymentMethodInfo#method](http://dev.commercetools.com/http-api-projects-payments.html#paymentmethodinfo))
  must be **`installment`**, see [CtpPaymentMethods#INSTALLMENT](/src/main/java/com/commercetools/payment/constants/ctp/CtpPaymentMethods.java)
  
  3. The respective cart (connected to the payment) must have customer's real **billing** and **shipping** addresses - 
  they will be verified by Installment (Rate) system.
      * Billing Address is send during create payment, **it is not recommended to send during patch** (e.g. 
      don't patch address after success redirect)
      * Shipping address is under seller’s protection, so **this should NOT be changed** (e.g. not patched at all)
  
  4. After redirect to success page - look up the payment (get payment by ID) and if there are inconsistencies 
  (different amount, address, line items etc) -  reinitialize the whole payment process.
  
  5. If everything is fine - show total payment details to the customer, including additional installment (rate) fees:
      * `credit_financing_offered:`
          * `total_interest.value`
          * `total_cost.value` 
          * `term`
          * `monthly_payment.value`
          
  6. See more in _Integration Requirements for Installments Germany_ internal document.
  
  

