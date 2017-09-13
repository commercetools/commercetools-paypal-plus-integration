# commercetools _Paypal Plus_ Service Integration Guide

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Setup docker container](#setup-docker-container)
- [Preparing Paypal Plus accounts](#preparing-paypal-plus-accounts)
- [Preparing commercetools Platform accounts](#preparing-commercetools-platform-accounts)
- [Front-end workflow](#front-end-workflow)
- [Create default payments](#create-default-payments)
  - [Create installment (Ratenzahlung) payments](#create-installment-ratenzahlung-payments)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

This documentation describes how to setup, start and use `commercetools-paypal-plus-integtration-java` service.

## Setup docker container

## Preparing Paypal Plus accounts

## Preparing commercetools Platform accounts

## Front-end workflow

## Create default payments

Defaults payments are _Credit Card_, _Paypal_, _Invoice_.

### Create installment (Ratenzahlung) payments

Installment payment type requires different workflow because of especial security requirements from Paypal Plus.

  1. CTP ([PaymentMethodInfo#method](http://dev.commercetools.com/http-api-projects-payments.html#paymentmethodinfo))
  must be **`installment`**, see [CtpPaymentMethods#INSTALLMENT](/src/main/java/com/commercetools/payment/constants/ctp/CtpPaymentMethods.java)
  
  2. The respective cart (connected to the payment) must have customer's real **billing** and **shipping** addresses - 
  they will be verified by Installment (Ratenzahlung) system.
      * Billing Address is send during create payment, **it is not recommended to send during patch** (e.g. 
      don't patch address after success redirect)
      * Shipping address is under seller’s protection, so **this should NOT be changed** (e.g. not patched at all)
  
  3. After redirect to success page - look up the payment (get payment by ID) and if there are inconsistencies 
  (different amount, address, line items etc) - reinitialize the whole payment process.
  
  4. If user accepts the conditions of the payment and clicks approval button - execute payment like in normal workflow.
          
  5. See more in _Integration Requirements for Installments Germany_ internal document.
  
  

