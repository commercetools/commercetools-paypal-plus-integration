# commercetools _Paypal Plus_ Integration Service

## Local debug

```bash
./gradlew bootRun
```

Local _Run/Debug_ setting may be configured in [`/src/main/resources/config/application.yml`](/src/main/resources/config/application.yml) 
file. If the file exists - it overrides default [`/src/main/resources/application.yml`](/src/main/resources/application.yml).


## Tests

### Integration tests

Local integration test setting may be configured in [`/src/test/resources/config/application.yml`](/src/test/resources/config/application.yml) 
file. If the file exists - it overrides main context config and config from  [`/src/test/resources/application.yml`](/src/test/resources/application.yml).

**Note: keep this settings different from `/src/main/resources/config/application.yml`, 
which is used for local run/debug, because the integration tests will remove all data form the CTP project**


## How to use
In this process, there are 3 parties involved:
* The frontend -  the browser part of the shop. This is what the user interacts with.
* The backend - the shop server.
* Paypal-integration - hosted service (this repository) which exposes public endpoints 

1. Show available PayPal payment methods
    1. Backend creates CTP payment and assigns it to the cart. 
        - Required fields for the CTP cart:
            - There must be at least 1 line or custom line item defined
            - Cart total amount should be > 0
            - Shipping address should be set
        - Required fields for the CTP payment:
            - amountPlanned
            - cancelUrl
            - successUrl
            - paymentMethodInfo needs to be set like this:
            ```json
            "paymentMethodInfo": {
              "paymentInterface": "PAYPAL_PLUS",
              "method": "paypal"
            }
            ```
    1. Backend POSTs CTP payment ID created in the previous step to Paypal-integration. Example: 
        ```
        POST http://paypal-plus-integration-server.com/${tenantName}/commercetools/create/payments/${ctpPaymentId}
        ```
        If request was successful both response body and CTP payment object will have `approvalUrl` defined.
    1. Frontend uses returned `approvalUrl` to render available payment methods as described in the Paypal Plus integration documentation.

2. Add user's addresses to Paypal Plus
    1. Before redirect the user to Paypal, backend POSTs CTP payment ID to Paypal-integration:
        ```
        POST http://paypal-plus-integration-server.com/${tenantName}/commercetools/patch/payments/${ctpPaymentId}
        ```
    2. NOTICE: refer to the newest version of Paypal Plus Integration documentation to know how to make a request
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

3. Execute payment after user successfully finished PayPal Plus checkout and was redirected back to the shop through `successUrl`.
    PayPal Plus will set 3 request parameters to `successUrl`:
    - `token`
    - `paymentId` - identifies this particular payment. **Required for execute payment.**
    - `PayerID` - identifies the particular payer. **Required for execute payment.**
    
    Example of `successUrl` returned by PayPal Plus: 
    ```
    http://example.com/checkout/payment/success?paymentId=${paymentId}&token=${token}&PayerID=${payerId} 
    ```
    1. Backend extracts PayPal specific parameters: `paymentId`, `PayerID` and POSTs them to Paypal-integration for payment execution. Example:
    ```
    POST http://paypal-plus-integration-server.com/${tenantName}/commercetools/execute/payments/
    {"paypalPlusPaymentId": "${paymentId}", "paypalPlusPayerId": "${payerId}"}
    ```
    2. In case of **invoice payment**, the bank details for the invoice will be saved as custom fields in the Payment object. Example:
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
    
## HTTP Responses
All endpoints accept and return data as JSON.

1. Return HTTP codes on `create/payments` endpoint URL:
- **201**: successfully created payment in PayPal and CTP updated with approvalUrl as custom field
- **404**: resource not found by the supplied UUID/ID
- **400**: required request parameters are missing or wrong
- **503**: any exception which implies that request can be safely retried with the same parameters/payload again
- **500**: unexpected/not handled exceptions

1. Return HTTP codes on `execute/payments` endpoint URL:
- **201**: successfully executed payment in PayPal, created transaction in CTP
- **404**: resource not found by the supplied UUID/ID
- **400**: required request parameters are missing or wrong
- **503**: any exception which implies that request can be safely retried with the same parameters/payload again
- **500**: unexpected/not handled exceptions

Additionally, response can contain additional response body. All fields of the response body are optional. Example:
```json
{
  "message": "Successful processing",
  "approvalUrl": "https://test.de",              # applicable only in case of create payment
  "error": "",                                   # only in case of error and represents a unique error code
  "errorDescription": "Parameter 'x' is missing" # only in case of error
}
```
