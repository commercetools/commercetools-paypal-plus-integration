# Usecases tests

Complex tests which demonstrate the main service workflow. These tests should be run after the 
[unit](/src/test/unit/README.md) and [integration](/src/test/integration/README.md) tests are finished successfully.

The tests these tests are executed over running HTTP service (docker container) and perform real URL requests. 
It is generally recommended to use as least as possible mocking and stubbing here, but the real data. 

**NOTE**: _Work In Progress_, e.g. we will be migrating step-by-step the payment integration tests from 
[integration](/src/test/integration/) sources here.

Implementation Notes:
  - https://bmuschko.com/blog/docker-integration-testing/
  - https://github.com/bmuschko/gradle-docker-plugin
  
