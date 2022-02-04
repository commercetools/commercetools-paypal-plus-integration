# commercetools _Paypal Plus_ Service Integration Guide

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->

- [Setup docker container](#setup-docker-container)
- [Preparing Paypal Plus accounts](#preparing-paypal-plus-accounts)
- [Preparing commercetools Platform accounts](#preparing-commercetools-platform-accounts)
- [Configuration](#configuration)
- [CTP custom types synchronization on startup](#ctp-custom-types-synchronization-on-startup)
- [Prevent shipping (delivery) address change in Paypal Payment Dialog](#prevent-shipping-delivery-address-change-in-paypal-payment-dialog)
- [Create default payments](#create-default-payments)
  - [Create installment (Ratenzahlung) payments](#create-installment-ratenzahlung-payments)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

This documentation describes how to setup, start and use `commercetools-paypal-plus-integtration-java` service.

## Setup docker container

## Preparing Paypal Plus accounts

## Preparing commercetools Platform accounts

  - create *`payment-paypal`* custom type with fields, specified in 
  [ctPaymentCustomType.json](/src/main/resources/referenceModels/ctPaymentCustomType.json). See also 
  [CtpPaymentCustomFields](/src/main/java/com/commercetools/payment/constants/ctp/CtpPaymentCustomFields.java) class.

## Configuration
Application on startup will try to load the required configuration as environment variable named "SPRING_APPLICATION_JSON" :
```
SPRING_APPLICATION_JSON='{
  "tenantConfig": {
    "tenants": {
      "my-commercetools-projectkey1": {
        "ctp": {
          "projectKey": "my-commercetools-projectkey1",
          "clientId": "xxx",
          "clientSecret": "xxx"
        },
        "paypalPlus": {
          "id": <your-paypal-plus-id>,
          "secret": <your-paypal-plus-secret>,
          "mode": "sandbox"
        }
      },
      "my-commercetools-projectkey2": {
        "ctp": {
          "projectKey": "my-commercetools-projectkey2",
          "clientId": "xxx",
          "clientSecret": "xxx"
        },
        "paypalPlus": {
          "id": <your-paypal-plus-id>,
          "secret": <your-paypal-plus-secret>,
          "mode": "sandbox"
        }
      }
    }
  },
  "ctp.paypal.plus.integration.server.url": <The URL of this deployed paypal integration application>
}'
```



## CTP custom types synchronization on startup

The service itself expects some required [CTP Types](http://dev.commercetools.com/http-api-projects-types.html)
configured on the tenants' projects. Since version [0.2.0](https://github.com/commercetools/commercetools-paypal-plus-integration/releases/tag/v0.2.0)
it is possible to perform automatic custom types creating/updating if some values are missing. 

The expected types configurations are stored in the project resources directory 
[**/src/main/resources/ctp/types/**](/src/main/resources/ctp/types/)  

When the service is starting, it validates the actual custom types of every tenant:
  - if at least one custom type of at least one tenant is misconfigured and can't be updated automatically &mdash; 
  the service will log the error message and exit
  - if some tenants/types could be automatically updated &mdash; update them and continue the service
  
In case of _Unrecoverable_ errors (which cause service exit) the respective CTP project must be re-configured manually
according to expected types configuration described above.
  
_Unrecoverable_ errors are:
  - actual [`resourceTypesIds`](http://dev.commercetools.com/http-api-projects-types.html#type) list is missing some of
  expected [resource ids](http://dev.commercetools.com/http-api-projects-custom-fields.html#customizable-resources)
  - [Field Definition](http://dev.commercetools.com/http-api-projects-types.html#fielddefinition) 
  with the same name have different 
  [field definition type](http://dev.commercetools.com/http-api-projects-types.html#fieldtype)
  - `FieldDefinition#required` property of the actual field definition is not equal to expected value
  - [`ReferenceType#referenceTypeId`](http://dev.commercetools.com/http-api-projects-types.html#referencetype) of the 
  actual field is not equal to expected value
  - [`SetType#elementType`](http://dev.commercetools.com/http-api-projects-types.html#settype) of the actual field 
  is not equal to expected value

If the projects don't have any unrecoverable errors - try to find and fix _recoverable_ mismatches.

_Recoverable_ mismatches of the types configurations are:
  - some types are completely missing &mdash; create them from scratch
  - some field definitions in a Type are missing &mdash; add missing fields to respective Types
  - [`EnumType`](http://dev.commercetools.com/http-api-projects-types.html#enumtype)/[`LocalizedEnumType`](http://dev.commercetools.com/http-api-projects-types.html#localizedenumtype)
  fields have some missing values (key names) - add missing enum entries.

All redundant/superfluous Types/Field Definitions/Enum Values are ignored and remain unchanged.
  
Also, note, some `Type` and `FieldDefinition` values are not important for the server, 
so they are never verified/updated 
(for example, Type _name_, Type _description_; Field Definition _labels_, _input hints_ and so on).

If all the types synced successfully or already up-to-date - the service continues starting up.

## Prevent shipping (delivery) address change in Paypal Payment Dialog

There is still not clear what is correct/recommended/document way to prevent buyer's address change on the
approval page (when the buyer is redirected). As discussed with support (Kristian Büsch) there are 2 possible solutions:
  1. (Recommended, but not documented yet): using `Payment#application_context#shipping_preference` value. The field
  should be set to `SET_PROVIDED_ADDRESS` value.
   
      **Note**:
      - use `shippingPreference` enum custom field in CTP payment to create payment with such behavior 
      (set the field to `SET_PROVIDED_ADDRESS` value)
      
      - the **shipping address must be set** before user is redirected to the approval page. 
      If this is a default payment type - `patch` endpoint should be used after payment is created, 
      but before buyer is redirected to the approval page.
      
      - unfortunately, this property is neither documented on the API nor implemented by SDK. Respective issues created:
        - https://github.com/paypal/PayPal-Java-SDK/issues/330
        - https://github.com/paypal/PayPal-REST-API-issues/issues/179
        - https://github.com/paypal/PayPal-REST-API-issues/issues/181
        
        Meanwhile we are using [Orders API](https://developer.paypal.com/docs/api/orders/#definition-application_context)
      documentation
      
  2. (Documented, but not recommended way since it could be deprecated soon): using 
  [payments experience profile](https://developer.paypal.com/docs/api/payment-experience/).
    
      1. [Create payment experience profile in the Paypal Merchant account](https://developer.paypal.com/docs/api/payment-experience/#web-profiles_create)
      with `input_fields#address_override=1`
      (Note, our _commercetools-paypalplus-integration_ service does not expose such endpoint, it should be created directly
      using Paypal API)
      
      2. Set the id of created web profile to `experienceProfileId` custom field of every payment handled by our service.
      
      3. The **shipping address must be set** before user is redirected to the approval page. 
               If this is a default payment type - `patch` endpoint should be used after payment is created, 
               but before buyer is redirected to the approval page.
      
  Right now our _commercetools-paypalplus-integration_ service support both ways, so any of properties described above
  will be sent to PayPal Plus API if it is specified in a CTP Payment custom field.

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
  
  

