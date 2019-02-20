# Integration tests

In this directory we keep complex slow integration tests, which:
  
  - require Spring specific dependencies and beans injection (like autowiring and so on).
  
  - require Spring context configuration (usual run with `@SpringBootTest` and similar approaches)
  
  - require application configuration 
  (e.g. tenants configuration, credentials to _CTP_ and _PayPal Plus_ staging/sandbox environments 
  from `application.yml` and so on)
  
  - mocking/spying/stubbing should be used as less as possible, only in cases where some real calls can't be performed
  (for example, when sandbox environment doesn't allow some actions).
  
  - default testing profile is: **`testIntegrationProfile`** (configured in `application.yml` by default)
  
In the build workflow these tests should be executed only in case the unit tests were success.
  
See [unit tests](/src/test/unit/README.md)

**Note**:

It is recommended in the future to split these tests into two different levels:
  
  - ***spring-integration-tests***, where tested Spring service specific cases, 
  e.g. just verify beans injection/interaction, application configuration, start-up workflow, controllers and so on.
  In general these tests should contain as least as possible tests which use:
    - `MockMvc` and `MockMvcAsync`
    - `TestRestTemplate`
    - web environment and `@LocalServerPort`
    
      Most of such cases should go to **usecase-tests**
  
  - ***usecase-tests***, where real payments use-cases are tested, 
  e.g. payment is create/handled/notification processed and so on. These tests should contain cases which our customers
  use. Also, these tests could be referred in the future for new customers who just want to apply the payment system
  to theirs shops and want to investigate how it works.