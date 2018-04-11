package com.commercetools.config;

import com.commercetools.config.bean.CtpConfigStartupValidator;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Mandatory "blocking" configuration to verify on start-up that CTP custom types exist and have proper fields configured.
 * <p>
 * <b>Note:</b> {@link PaypalPlusStartupConfiguration} explicitly depends on this configuration, thus won't proceed
 * if some CTP types are misconfigured.
 */
@Configuration
public class CtpConfigStartupConfiguration {
    private final CtpConfigStartupValidator ctpConfigStartupValidator;

    public CtpConfigStartupConfiguration(CtpConfigStartupValidator ctpConfigStartupValidator) {
        this.ctpConfigStartupValidator = ctpConfigStartupValidator;
    }

    @PostConstruct
    private void init() {
        ctpConfigStartupValidator.validateTypes();
    }
}
