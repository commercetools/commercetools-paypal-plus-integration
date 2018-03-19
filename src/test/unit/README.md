# Unit tests

In this directory we keep simple fast unit tests, which:
  
  - don't require Spring specific dependencies and beans injection (like autowiring and so on).
  
    In the unit tests dependencies are used to be injected directly by constructors, setters and properties
  
  - don't require real services and staging systems configuration
  
  - don't require Spring context configuration
  
  - don't require application configuration or profiles
  
  - mocking/spying/stubbing are preferred ways to test in these tests.
  
In common build workflow it is recommended to run the unit tests before integration, 
and fail the entire build skipping integration if unit tests failed (_fail-fast_)
  
See for more complex [integration tests](/src/test/integration/README.md) workflow.