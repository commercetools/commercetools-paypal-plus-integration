package com.commercetools.config.bean.impl;

import com.commercetools.config.bean.ApplicationKiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * This implementation of {@link ApplicationKiller} is hard and rough: it calls directly {@code System.exit(code)},
 * but not {@code System.exit(SpringApplication.exit(applicationContext, () -> code));}, because the last one could
 * be blocked by Spring application context so the application is actually not exited.
 */
public class ApplicationKillerImpl implements ApplicationKiller {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationKillerImpl.class);

    @Override
    public void killApplication(int exitCode, @Nonnull String message) {
        LOGGER.error("Application exit with code {}. Exit message: {}", exitCode, message);
        exitApplication(exitCode);
    }

    @Override
    public void killApplication(int exitCode, @Nonnull Throwable reason) {
        LOGGER.error("Application exit with code {}. Reason: {}", exitCode, reason);
        exitApplication(exitCode);
    }

    @Override
    public void killApplication(int exitCode, @Nonnull String message, @Nonnull Throwable reason) {
        LOGGER.error("Application exit with code {}. Exit message: {}\nReason: ", exitCode, message, reason);
        exitApplication(exitCode);
    }

    private void exitApplication(int exitCode) {
        System.exit(exitCode);
    }


}
