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
    See [ctPaymentCustomType.json](/src/main/resources/referenceModels/ctPaymentCustomType.json) for full custom type 
    description.
