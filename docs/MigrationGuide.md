<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Migration Guide](#migration-guide)
  - [To v0.2+](#to-v02)
  - [To v0.3+](#to-v03)
    - [To v0.4.0](#to-v040)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# Migration Guide

## To v0.2+

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
  
  - See [payment-paypal.json](/src/main/resources/ctp/types/payment-paypal.json) for full custom type description.


## To v0.3+
<!-- 
    NOTE: This chapter is referenced in CtpConfigStartupValidator#DOCUMENTATION_REFERENCE, 
    so update the log message in case of re-factoring the documentation
-->

  - ensure CTP custom Type `payment-paypal` has `successUrl` and `cancelUrl` field definitions as mandatory 
  (`"required": true`). If the type/fields are missing - just skip this step, because they will by synced automatically.
  If the fields exist, but are not _required_ - update them. 
  The easiest way is to remove them right before service start - they will be created automatically 
  by project types sync feature. 
  
    To remove the field execute the next payload (with actual `version` value from your project):
    ```json
    {
      "version": CURRENT_payment-paypal_OBJECT_VERSION,
      "actions": [
        {
          "action": "removeFieldDefinition",
          "fieldName": "successUrl"
        },
        {
          "action": "removeFieldDefinition",
          "fieldName": "cancelUrl"
        }
      ]
    }
    ```
    
    **This approach (remove a field definition before service start) could be applied to any field definition, 
    which has unexpected _required_ field - the field will be re-created automatically by sync CTP types feature.**

### To v0.4.0

After new service version deployment new custom field will be added automatically to `payment-paypal`
custom type. After that just specify `description` custom field on payment creation if you want to have
custom payment description. Leave the field undefined (or null) 
to fallback to default description with payment reference:

`Reference: ${payment#custom#reference}`

**Note**: according to [PayPal Plus documentation](https://developer.paypal.com/docs/api/payments/#definition-transaction)
length of the description must be up to 127 characters, thus length of `reference` must be up to 116 characters.

### To v0.8.0

New service version is capable of adding prefix for product name of paypal `Item`. Prefix is constructed 
through a product attribute. Attribute can be configured through `prefixProductNameWithAttr` optional 
configuration property in application.yml file.

**Note**: according to [PayPal Plus documentation](https://developer.paypal.com/docs/api/payments/v1/#definition-item)
length of the product name must be up to 127 characters.
