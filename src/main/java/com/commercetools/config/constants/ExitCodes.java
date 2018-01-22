package com.commercetools.config.constants;

import com.commercetools.config.CtpConfigStartupValidator;
import io.sphere.sdk.types.Type;

/**
 * Application error exit codes.
 */
public final class ExitCodes {

    /**
     * If CTP {@link Type}s are incompatible with expected types and can't be automatically synchronized by the service.
     *
     * @see CtpConfigStartupValidator
     */
    public static final int EXIT_CODE_CTP_TYPE_INCOMPATIBLE = 129;

    /**
     * If unexpected exception occurred in CTP {@link Type}s validation and synchronization.
     *
     * @see CtpConfigStartupValidator
     */
    public static final int EXIT_CODE_CTP_TYPE_VALIDATION_EXCEPTION = 130;

    private ExitCodes() {
    }
}
