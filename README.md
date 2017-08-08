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

2. Execute payment after user successfully finished PayPal Plus checkout and was redirected back to the shop through `successUrl`.
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