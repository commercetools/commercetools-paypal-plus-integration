<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Migration Guide](#migration-guide)
- [From v0.1 to v0.2](#from-v01-to-v02)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Migration Guide

# From v0.1 to v0.2

  - add new custom field `experienceProfileId` to `payment-paypal` CTP custom type:
      ```json
      {
        "name": "experienceProfileId",
        "label": {
          "de": "experienceProfileId",
          "en": "experienceProfileId"
        },
        "required": false,
        "type": {
          "name": "String"
        },
        "inputHint": "SingleLine"
      }
      ```
      
  - add new custom field `shippingPreference` to `payment-paypal` CTP custom type:
      ```json
      {
        "name": "shippingPreference",
        "label": {
          "en": "shippingPreference"
        },
        "required": false,
        "type": {
          "name": "Enum",
          "values": [
            {
              "key": "NO_SHIPPING",
              "label": "Redacts shipping address fields from the PayPal pages. Recommended value to use for digital goods."
            },
            {
              "key": "GET_FROM_FILE",
              "label": "Get the shipping address selected by the buyer on PayPal pages."
            },
            {
              "key": "SET_PROVIDED_ADDRESS",
              "label": "Use the address provided by the merchant. Buyer is not able to change the address on the PayPal pages. If merchant doesn't pass an address buyer has the option to choose the address on PayPal pages."
            }
          ]
        },
        "inputHint": "SingleLine"
      }
      ```
  
  - See [ctPaymentCustomType.json](/src/main/resources/referenceModels/ctPaymentCustomType.json) for full custom type 
    description.
