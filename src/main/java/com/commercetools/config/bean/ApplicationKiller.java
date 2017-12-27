package com.commercetools.config.bean;

import javax.annotation.Nonnull;

/**
 * Bean to perform Spring application stopping (killing).
 * <p>
 * Use this bean to inject/mock/spy/verify during the tests instead of real application killing.
 */
public interface ApplicationKiller {
    void killApplication(int exitCode, @Nonnull String message);

    void killApplication(int exitCode, @Nonnull Throwable reason);

    void killApplication(int exitCode, @Nonnull String message, @Nonnull Throwable reason);
}
