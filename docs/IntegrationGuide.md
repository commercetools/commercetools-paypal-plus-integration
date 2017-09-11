# commercetools _Paypal Plus_ Service Integration Guide

This documentation describes how to setup, start and use `commercetools-paypal-plus-integtration-java` service.

## Setup docker container

## Preparing Paypal Plus accounts

## Preparing commercetools Platform accounts

## Front-end workflow

## Create default payments

Defaults payments are _Credit Card_, _Paypal_, _Invoice_.

### Create installment (rate) payments

Installment payment type requires different workflow because of especial security requirements from Paypal Plus.

  1. CTP ([PaymentMethodInfo#method](http://dev.commercetools.com/http-api-projects-payments.html#paymentmethodinfo))
  must be **`installment`**, see [CtpPaymentMethods#INSTALLMENT](/src/main/java/com/commercetools/payment/constants/ctp/CtpPaymentMethods.java)
  2. The respective cart (connected to the payment) must have customer's real **billing** and **shipping** addresses - 
  they will be verified by Installment (Rate) system.
