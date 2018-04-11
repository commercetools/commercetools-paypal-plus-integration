package com.commercetools.config.bean;

import io.sphere.sdk.types.Type;

/**
 * Verify CTP project settings are correct for all configured tenants. If the CTP project settings can't be
 * automatically synced/updated - the errors are logged and the service exits.
 *
 * @see #validateTypes()
 */
public interface CtpConfigStartupValidator {

    /**
     * Verify the CTP {@link Type}s are configured like expected. The implementation compares actual tenant
     * {@link Type}s with expected types from the embed resources
     * (see {@link com.commercetools.config.ctpTypes.ExpectedCtpTypes}). If the types could be updated -
     * the update actions are performed. Otherwise error message is shown and application is finished with error code.
     * <p>
     * If neither errors exist nor update actions required - continue application startup.
     * <p>
     * The operation is blocking, e.g. the application startup shall not go further before this
     * validation/synchronization is done completely.
     */
    void validateTypes();
}
