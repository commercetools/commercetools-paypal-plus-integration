# Unit tests

In this directory we keep simple fast unit tests, which:
  
  - don't require Spring specific dependencies and beans injection (like autowiring and so on).
  
    In the unit tests dependencies are used to be injected directly by constructors, setters and properties
  
  - don't require real services and staging systems configuration
  
  - don't require Spring context configuration
  
  - don't require application configuration or profiles
  
  - mocking/spying/stubbing are preferred ways to test in these tests.
  
  - unit tests should run successfully without internet connection

**Note**:
  - Some utility classes (helpers, mappers, formatters and so on) are implemented as Spring components and require
  other components wiring, but in the same time they are simple enough to test them without full application context
  loading. In this case it still has sense to put such tests into _unit_ set 
  with `@SpringBootTest` and/or `@RunWith(SpringRunner.class)` runner, but don't configure and run whole `Application` - 
  just inject expected specific classes/components/mocks into the test context.
  
  See examples of such Spring unit tests: [`PrettyGsonMessageConverterTest`](/src/test/unit/java/com/commercetools/http/converter/json/PrettyGsonMessageConverterTest.java),
[`DefaultPaymentMapperImplTest`](/src/test/unit/java/com/commercetools/helper/mapper/impl/payment/DefaultPaymentMapperImplTest.java),
[`InstallmentPaymentMapperImplTest`](/src/test/unit/java/com/commercetools/helper/mapper/impl/payment/InstallmentPaymentMapperImplTest.java),
[`PaymentMapperHelperImplTest`](/src/test/unit/java/com/commercetools/helper/mapper/impl/PaymentMapperHelperImplTest.java),
[`PaypalPlusFormatterImplTest`](/src/test/unit/java/com/commercetools/helper/formatter/impl/PaypalPlusFormatterImplTest.java)
     

In common build workflow it is recommended to run the unit tests before integration, 
and fail the entire build skipping integration if unit tests failed (_fail-fast_).
  
See for more complex [integration tests](/src/test/integration/README.md) workflow.