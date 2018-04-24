Complex tests which demonstrate the main service workflow. These tests should be run after the 
[unit](/src/test/unit/README.md) and [integration](/src/test/integration/README.md) tests are finished successfully.

These tests are executed over running HTTP service (docker container) and perform real URL requests. 
It is generally recommended to use as least as possible mocking and stubbing here, but the real data. 

**NOTE**: _Work In Progress_, e.g. we will be migrating step-by-step the payment integration tests from 
[integration](/src/test/integration/) sources here. In general at the end we should move all (or most of them) 
the tests which depend on CTP projects and PayPal sandbox to this tests set.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Documentation](#documentation)
- [How to start usecases tests locally](#how-to-start-usecases-tests-locally)
  - [From command line](#from-command-line)
  - [From IDE](#from-ide)
  - [Local tests development](#local-tests-development)
- [Notes](#notes)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Documentation

  - https://bmuschko.com/blog/docker-integration-testing/
  - https://github.com/bmuschko/gradle-docker-plugin
  
## How to start usecases tests locally
  
### From command line
    
```bash
./gradlew clean buildDockerImage testUsecases
``` 

optionally supply `-x test -x testIntegration` if you wish to skip these tests.

This command builds docker image, runs it and executes the tests.
  

### From IDE

1. Build docker image, but don't run it. One could do it either using native installed docker tools 
or gradle tasks:

```bash
./gradlew clean buildDockerImage
```

In IDE run docker container and execute the tests using gradle task _testUsecases_. 
For example, in IDE you could use context menu to _Run/Debug_ certain test classes.

**Note**: for some (configuration?) reason _Intellij IDEA_ doesn't properly recognize the task automatically by
test sources set, thus if you use context menu to _Run/Debug_ classes - it starts _test_ task instead of _testUsecases_
task. 
The solution could be:
  - either run the task explicitly from _Gradle projects -> Tasks -> other -> testUsecases_
  - or when you run from context menu edit newly created _Run/Debug Configuration_ and change the tasks from
  _:clean :test_ to _:cleanTestUsecases :testUsecases_ 
    
    ![Intellij IDEA Run/Debug Config](ideaRunDebugConfig.png?raw=true "Usecases execution in Run/Debug Configuration")
    
### Local tests development

Sometimes when you refactor local usecase tests it is not necessary to create/destroy the container on every test run
(and it is quite slow). So, during development phase it is recommended to create/start a container 
and just the tests over existing container.

  1. Create and run the container:
  
      ```bash
      export SPRING_APPLICATION_JSON='PUT_UT_ENVIRONMENT_CREDENTIALS_HERE'
      ./gradlew buildAndRunDocker
      # verify running service
      docker ps --filter="name=commercetools-paypalplus-integration-test-container"
      curl -v http://localhost:8080/health
      ```
  
  2. Run specific tests (likely from IDE):
      ```bash
      ./gradlew :cleanTestUsecases :testUsecases --tests com.commercetools.payment.handler.CommercetoolsCreatePaymentsControllerUT
      ``` 
      
  3. Stop/remove the container (after all tests are done, of testing image should be rebuilt):
      ```bash
      docker kill $(docker ps -q --filter="name=commercetools-paypalplus-integration-test-container")
      # optionally clean-up the garbage of stopped containers
      docker rm $(docker ps -a -q --filter="name=commercetools-paypalplus-integration-test-container")
      ```
  
## Notes
  
- don't forget to pass `SPRING_APPLICATION_JSON` or explicitly specify `application.yml`
to running the test container the in remote (Travis) builds, otherwise the container
stops immediately because of empty mandatory configuration (tenants).
  
  - **Note**: if you build the image locally (either over gradle _buildDockerImage_ task or native docker build) 
  the local secret file `/src/main/java/resources/config/application.yml`
  is also included to the built image! 
    
    ***Thus don't publish this image***.
  
    But in the same time such approach (including local secrets) could be useful to run container locally 
    without `SPRING_APPLICATION_JSON` evn variable.
    
## TODO:

**The tests are not completed. Things to be done:**
  
  - move more specific usecases from [_integration (payment)_](/src/test/integration/java/com/commercetools/payment)
  to these sources
  
  - implement full workflow simulating customer's payment approval 
  (likely using Selenium test or something similar to navigate to PayPal page and approve the payment)
