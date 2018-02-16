<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Setup docker container](#setup-docker-container)
- [Preparing Paypal Plus accounts](#preparing-paypal-plus-accounts)
- [Preparing commercetools Platform accounts](#preparing-commercetools-platform-accounts)
- [Promoting Release From Staging to Production environment](#promoting-release-from-staging-to-production-environment)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

This documentation describes how to setup, start and use `commercetools-paypal-plus-integration-java` service.

## Setup docker container

## Preparing Paypal Plus accounts

## Preparing commercetools Platform accounts

## Promoting Release From Staging to Production environment

When going live from staging version ensure the following settings:
  
  - ensure service release version (e.g. docker tag).
    Sometimes staging setup uses `:latest` version, 
    when production uses explicit product version, 
    so ensure on production setup you have expected and up-to-date service version. 
    
    The latest versions, release and migration notes could be found on the 
    [Releases Page](https://github.com/commercetools/commercetools-paypal-plus-integration/releases) and
    [Migration Guide](/docs/MigrationGuide.md)
  
  - ensure service configuration (usually these values are configured in `SPRING_APPLICATION_JSON` environment variable):
    - set proper PayPal Plus settings for each tenant (`tenantConfig.tenants.[*].paypalPlus.*`):
      - `mode`: must be `live`
      - verify `id` and `secret`. 
      
        Note, when you switch [PayPal Plus application](https://developer.paypal.com/developer/applications/) 
        between `sanbox`/`live` - **both _Client ID_ and _Secret_ a changed!**
    
    - update [CTP project settings](https://admin.commercetools.com/): 
    ensure CTP `projectKey`, `clientId` and `clientSecret` are from production CTP project.
     
    - actualize `ctp.paypal.plus.integration.server.url`: set it to proper production HTTP**S** URL where the service is located. 
    This URL will be used to setup PayPal Plus webhooks (notifications)
  
  - (optional, but ***strongly recommended***) test the complete prod setup locally or on the staging environment 
  before going with it live
