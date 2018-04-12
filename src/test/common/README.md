# Common test sources

In this directory we keep classes which are reused for both unit and integration tests.
These are kind of test util methods and resources, like 
  - [`CompletionStageUtil#executeBlocking()`](/src/test/common/java/com/commercetools/testUtil/CompletionStageUtil.java)
  to perform blocking execution
  - [`CtpResourcesUtil`](/src/test/common/java/com/commercetools/testUtil/ctpUtil/CtpResourcesUtil.java) to create
  test/mock CTP objects.
  - and so on
 