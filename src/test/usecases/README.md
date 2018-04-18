# Usecases tests

Complex tests which demonstrate the main service workflow. These tests should be run after the 
[unit](/src/test/unit/README.md) and [integration](/src/test/integration/README.md) tests are finished successfully.

The tests these tests are executed over running HTTP service (docker container) and perform real URL requests. 
It is generally recommended to use as least as possible mocking and stubbing here, but the real data. 

**NOTE**: _Work In Progress_, e.g. we will be migrating step-by-step the payment integration tests from 
[integration](/src/test/integration/) sources here. In general at the end we should move all (or most of them) 
the tests which depend on CTP projects and PayPal sandbox to this tests set.

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
