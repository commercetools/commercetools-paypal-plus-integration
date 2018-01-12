# Shop integration guide

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Integration of payment into checkout process](#integration-of-payment-into-checkout-process)
  - [Glossary](#glossary)
  - [Checkout steps](#checkout-steps)
  - [Validations](#validations)
    - [Validate cart state](#validate-cart-state)
    - [Recalculate cart](#recalculate-cart)
    - [Validate payment amount](#validate-payment-amount)
    - [Validate payment transaction](#validate-payment-transaction)
  - [Possible edge cases](#possible-edge-cases)
  - [Bad practice](#bad-practice)
- [PayPal integration service API](#paypal-integration-service-api)
  - [HTTP Responses](#http-responses)
- [Supported payment types](#supported-payment-types)
  - [Default payments](#default-payments)
  - [Installment (Ratenzahlung) payment](#installment-ratenzahlung-payment)
- [Prevent shipping (delivery) address change in Paypal Payment Dialog](#prevent-shipping-delivery-address-change-in-paypal-payment-dialog)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Integration of payment into checkout process

### Glossary
In this process, there are 3 parties involved:
* **Frontend** -  the browser part of the shop. This is what the user interacts with.
* **Backend** - the shop server which supplies front end with data.
* **Paypal-integration** - hosted service (this repository) which exposes public endpoints.

### Checkout steps
1. **On each checkout step [validate cart state](#validate-cart-state)**

1. **Before starting payment process make sure there is no valid payments already**:
  - [Recalculate cart](#recalculate-cart)
  - [Validate payment amount](#validate-payment-amount)
  - [Validate payment transaction](#validate-payment-transaction)

  If all above validations are passed then order can be created right away and order confirmation page shown. Otherwise user might continue with further payment steps.

1. **Show available PayPal payment methods**

    1. Backend creates CTP payment object and assigns it to the cart.
        - CTP cart requirements:
            - There must be at least 1 line or custom line item defined
            - Cart's total amount should be greater than `0`
            - Shipping address should be set

        - Required fields for the CTP payment object:
            - `amountPlanned`
            - `cancelUrl` (custom field)
            - `successUrl` (custom field)
            - `paymentMethodInfo` needs to be set like this:
            ```json
            "paymentMethodInfo": {
                "paymentInterface": "PAYPAL_PLUS",
                "method": "paypal"
            }
            ```
        - Optional fields for the CTP payment:
            - `experienceProfileId` (custom field): if the payment should be supplied with certain
            [Paypal Plus Experience Profile Id](https://developer.paypal.com/docs/api/payment-experience/).

              **Note**: looks like there is no certain clarity in PayPal Plus documentation/support
              how the web experience profiles should be used, and potentially they will be deprecated in the future,
              so we recommend not to use them unless you are completely sure what you achieve using them.
              If you need prevent address change by customers (buyers) - use `shippingPreference` below.

            - `shippingPreference` (custom field): value of `Payment#application_context#shipping_preference` enum.
            This value is used to allow/block shipping address change by the customer (buyer). Reed more at
            [Application Context Documentation](https://developer.paypal.com/docs/api/orders/#definition-application_context)

              **Note**: So far this feature is not properly documented by Paypal API developers, so the reference above
              actually refers to [Orders API](https://developer.paypal.com/docs/api/orders/#definition-application_context),
              instead of [Payments API](https://developer.paypal.com/docs/api/payments/#definition-application_context).
              Respective issues are created:
                - [Payment/Order#applicationContext property is not available](https://github.com/paypal/PayPal-Java-SDK/issues/330#issuecomment-356008914)
                - [Payment application_context is not documented](https://github.com/paypal/PayPal-REST-API-issues/issues/179)

    1. Backend POSTs CTP payment ID (created in the previous step) to Paypal-integration (see also [HTTP Responses](#http-responses)). Example:
        ```
        POST https://paypal-plus-integration-server.com/${tenantName}/commercetools/create/payments/${ctpPaymentId}
        ```
        If request was successful both response body as also CTP payment object (as custom field) will have `approvalUrl` defined.

    1. Frontend uses returned `approvalUrl` to render available payment methods within PayPal hosted iFrame. For more details see official [Paypal Plus integration guide](https://www.paypalobjects.com/webstatic/de_DE/downloads/PayPal-PLUS-IntegrationGuide.pdf).

1. **Submit user's addresses to Paypal Plus**

    After user's payment method selection and before redirection of the user to Paypal, backend has to POST CTP payment ID to Paypal-integration (see also [HTTP Responses](#http-responses)):
        ```
        POST https://paypal-plus-integration-server.com/${tenantName}/commercetools/patch/payments/${ctpPaymentId}
        ```
    **Note**: Refer to the newest version of [Paypal Plus integration guide](https://www.paypalobjects.com/webstatic/de_DE/downloads/PayPal-PLUS-IntegrationGuide.pdf) in order to make a request
    before redirect in Javascript. As of August 2017, on submit should call `ppp.doContinue()`. Additionally,
    `ppp` object must be created with the following option:
     ```javascript
         var ppp = PAYPAL.apps.PPP({
             onContinue: function () {
                   $.post("/url-to-your-shop-that-will-make-call-to-paypal-integration", function (data) {
                   if (data != false) {
                     ppp.doCheckout();
                   }
               });
             }
         });
     ```

1. **User successfully finished PayPal Plus checkout and was redirected back to the shop through `successUrl`**

  Before **payment execution** apply following validations:
  - [Validate cart state](#validate-cart-state)
  - [Recalculate cart](#recalculate-cart)
  - [Validate payment amount](#validate-payment-amount) - Should it fail then an error message should be shown and whole payment process repeated (see step 1). !Validate addres shipping cart items info and cart amount
  - [Validate payment transaction](#validate-payment-transaction)

  If all above validations are passed then order can be created right away and order confirmation page shown. . Otherwise user might continue with **payment execution**.

1. **Execute payment**.

    PayPal Plus will set 3 request parameters to `successUrl`:
    - `token`
    - `paymentId` - identifies particular PayPal payment. **Required for execute payment.**
    - `PayerID` - identifies the particular PayPal payer. **Required for execute payment.**

    Example of `successUrl` returned by PayPal Plus:
    ```
    http://example.com/checkout/payment/success?paymentId=${paymentId}&token=${token}&PayerID=${payerId}
    ```
    1. It is strongly recommended to compare the payment from PayPal to payment from CTP to see if there were any changes during the payment process.
     Example of how this can happen is described [here](#possible-edge-cases). The possible changes could be:
        1. User's shipping address has changed
        1. Cart total amount has changed

        Backend GET Paypal payment by `paypalPaymentId`:
        ```
        GET http://paypal-plus-integration-server.com/${tenantName}/paypalplus/payments/${paypalPaymentId}
        ```
        The Paypal Plus payment object will be returned in `payment` as JSON like this:
        ```json
        {"payment":"{\"id\":\"PAY-xxx\",\"intent\":\"sale\",\"cart\":\"1234abcd\", .... }"}
        ```

    1. Backend extracts PayPal specific parameters: `paymentId`, `PayerID` and POSTs them in the request body to Paypal-integration (see also [HTTP Responses](#http-responses)) for payment execution. Example:
    ```
    POST https://paypal-plus-integration-server.com/${tenantName}/commercetools/execute/payments/
    {"paypalPlusPaymentId": "${paymentId}", "paypalPlusPayerId": "${payerId}"}
    ```
    1. In case of **invoice payment**, the bank details for the invoice will be saved as custom fields in the Payment object. Example:
    ```json
    {
       "custom": {
        "type": {
          "typeId": "type",
          "id": "1455d4e6-41b4-yyyy-xxxx-4f120864e231"
        },
        "fields": {
          "reference": "6KF07542JV235932C",
          "paymentDueDate": "2017-09-27",
          "amount": {
            "centAmount": 200,
            "currencyCode": "EUR"
          },
          "paidToIBAN": "DE1212121212123456789",
          "paidToAccountBankName": "Deutsche Bank",
          "paidToAccountHolderName": "PayPal Europe",
          "paidToBIC": "DEUTDEDBPAL"
        }
      }
    }
    ```

### Validations

#### Validate cart state
Check if current cart has been ordered already (`Cart#cartState` = `Ordered`). In this case load order by ordered cart ID and show oder confirmation page. This might happen if cart has been already ordered in a different tab or by asynchronous process like [commercetools-payment-to-order-processor](https://github.com/commercetools/commercetools-payment-to-order-processor) job.

#### Recalculate cart
To ensure cart totals are always up-to-date execute cart [recalculate](https://dev.commercetools.com/http-api-projects-carts.html#recalculate). Time limited discounts are not removed/invalidated from cart automatically. They are validated on recalculate and order creation only.

#### Validate payment amount
There must be at least one CTP payment object of type PayPal Plus (`#Payment#paymentMethodInfo#paymentInterface` = `PAYPAL_PLUS`) where `Payment#amountPlanned` matches current cart's total amount and currency.

#### Validate payment transaction
Cart's payment counts as successful if there is at least one payment object with successful (`Payment#Transaction#state`=`Success`) payment transaction of type `Charge` and for the same payment object there is no other successful (`#Payment#Transaction#state`=`Success`) transactions of **other** type than `Charge` and `#Transaction#amount` greater than `0`.

### Possible edge cases
1. First case:
    1. User inputs an address Berlin, Germany
    1. User clicks on Continue and is redirected to Paypal payment page with this address.
    1. In a different tab, the same user changes his address to Paris, France.
    1. The user confirms the payment in the first tab and is redirected back to shop
    1. Paypal has the address of Berlin, but the shop will deliver the goods to Paris.

    **Possible solution:** the backend calls `patch/payments` endpoint every time user changes it.

1. Second case:
    1. User has cart with item1=10€
    1. User is redirected to Paypal payment page
    1. In a different tab, user changes his cart to e.g. item2=20€
    1. User completes the payment in the first tab and is redirected back to shop
    1. Paypal approves the payment for 10€, but the real total amount of sold (shipped) items has changed to 20€

    **Possible solution:** the backend has to compare total amount of the payment and total amount of payment's cart before calling `execute/payments` endpoint.
     In case of differences, the whole payment process must be restarted.

### Bad practice
- Never delete or un-assign created payment objects **during checkout** from the cart. If required &mdash; clean up  unused/obsolete payment objects by another asynchronous process instead.

## PayPal integration service API

### HTTP Responses
All endpoints accept and return data as JSON.

1. Return HTTP codes on `create/payments` endpoint URL:
- **201**: successfully created payment in PayPal and CTP updated with `approvalUrl` as custom field

1. Return HTTP codes on `execute/payments` endpoint URL:
- **201**: successfully executed payment in PayPal, created transaction in CTP

1. Return HTTP codes on `paypalplus/payments/${paypalPaymentId}` endpoint URL:
- **200**: Paypal payment found and returned as JSON response body

1. Common error codes
- **404**: resource not found by the supplied UUID/ID
- **400**: required request parameters are missing or wrong
- **503**: any exception which implies that request can be safely retried with the same parameters/payload again
- **500**: unexpected/not handled exceptions

Additionally, response can contain additional response body. All fields of the response body are optional. Example:
```json
{
  "approvalUrl": "https://test.de",              # applicable only in case of create payment
  "errorCode": "",                               # only in case of error and represents a unique error code
  "errorMessage": "Parameter 'x' is missing"     # only in case of error
  "payment": {"id":"XXX", "intent":"sale", ...}  # only in case of getting the payment object
}
```

## Supported payment types

### Default payments
Default payments are _Credit Card_, _Paypal_ and _Invoice_. In order to create any of default payments ensure that CTP payment object's payment method ([Payment#PaymentMethodInfo#method](http://dev.commercetools.com/http-api-projects-payments.html#paymentmethodinfo)) is set to **`default`**. See also [CtpPaymentMethods](/src/main/java/com/commercetools/payment/constants/ctp/CtpPaymentMethods.java).

### Installment (Ratenzahlung) payment

Installment payment type requires different workflow because of especial security requirements from Paypal Plus.

  1. CTP ([PaymentMethodInfo#method](http://dev.commercetools.com/http-api-projects-payments.html#paymentmethodinfo))
  must be **`installment`**, see [CtpPaymentMethods#INSTALLMENT](/src/main/java/com/commercetools/payment/constants/ctp/CtpPaymentMethods.java)

  2. The respective cart (connected to the payment) must have customer's real **billing** and **shipping** addresses -
  they will be verified by Installment (Ratenzahlung) system.
      * Billing Address is send during create payment, **it is not recommended to send during patch** (e.g.
      don't patch address after success redirect)
      * Shipping address is under seller’s protection, so **this should NOT be changed** (e.g. not patched at all)

  3. After redirect to success page - look up the payment (get payment by ID) and if there are inconsistencies
  (different amount, address, line items etc) - restart the whole payment process.

  4. If user accepts the conditions of the payment and clicks approval button - execute payment like in normal workflow.

  5. See more in _Integration Requirements for Installments Germany_ internal document.

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