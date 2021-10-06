package com.commercetools.util;

import io.sphere.sdk.client.SolutionInfo;

public final class PayPalSolutionInfo extends SolutionInfo {
  private static final String LIB_NAME = "commercetools-paypal-plus-integration";
  /** This value is injected by the script at gradle-scripts/set-library-version.gradle. */
  public static final String LIB_VERSION = "#{LIB_VERSION}";
  public static final String LIB_WEBSITE = "https://github.com/commercetools/commercetools-paypal-plus-integration";

  /**
   * Extends {@link SolutionInfo} class of the JVM SDK to append to the User-Agent header with
   * information of the commercetools-paypal-plus-integration library
   *
   * <p>A User-Agent header with a solution information looks like this: {@code
   * commercetools-paypal-plus-integration/1.46.1 (AHC/2.0) Java/1.8.0_92-b14 (Mac OS X; x86_64) {@value
   * LIB_NAME}/{@value LIB_VERSION}}
   */
  public PayPalSolutionInfo() {
    setName(LIB_NAME);
    setVersion(LIB_VERSION);
    setWebsite(LIB_WEBSITE);
  }
}
