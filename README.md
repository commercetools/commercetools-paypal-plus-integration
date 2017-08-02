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
* The integration server - this is the place where this project commercetools-paypal-plus-integration is deployed and running. 

1. In order to create a payment on PayPal Plus, backend must create first Payment object assigned to a cart. It must contain the following attributes:
- amountPlanned
- cancelUrl - this attribute is available from custom type. PayPal Plus redirects the user to this URL if he/she cancels the payment process.
- successUrl - this attribute is available from custom type. PayPal Plus redirects the user to this URL if he/she successfully completes the payment process.
- paymentMethodInfo needs to be set like this:
```json
   "paymentMethodInfo": {
        "paymentInterface": "PAYPAL_PLUS",
        "method": "paypal"
      }
```

The backend calls the following URL of the integration server to obtain `approval_url`.
```
POST http://paypal-plus-integration-server.com/${projectName}/commercetools/create/payments/${ctpPaymentId}
```
This `approval_url` should be passed to frontend to generate a list of available payment methods. Example response:
```
{
    "statusCode": 201,
    "approvalUrl": "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=${token}"
}
```

2. After the user pays, he/she gets redirected to the `successUrl` defined in step 1. Additionally, PayPal Plus will add 3 request parameters to this URL.
- token
- paymentId - identifies this particular payment. **Required for execute payment.**
- PayerID - identifies the particular payer.  **Required for execute payment.**   
Example of success URL returned by PayPal Plus: 
```
http://example.com/success/23456789?paymentId=${paymentId}token=${token}&PayerID=${payerId} 
```
The backend needs to call the integration server to patch and execute payment with **paymentId** and **PayerId** obtained from success URL.
Patch will update the user's address on PayPal and execute will finalize the payment process (money is transferred). After the payment is finalized, it cannot be patched anymore.
```
POST http://paypal-plus-integration-server.com/${projectName}/commercetools/execute/payments/

paypalPlusPayerId=${payerId}
paypalPlusPaymentId=${paymentId}
```
**NOTE - in the next few days, the logic will change and you will need to pass the parameters as JSON:**
```
{"paypalPlusPaymentId": "${paymentId}", "paypalPlusPayerId": "${payerId}"}
``` 
Example response on success:
```json
{
    "statusCode": 200
}
```