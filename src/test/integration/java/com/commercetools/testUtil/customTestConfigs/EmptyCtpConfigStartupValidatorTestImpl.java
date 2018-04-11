package com.commercetools.testUtil.customTestConfigs;

import com.commercetools.config.bean.CtpConfigStartupValidator;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Default <i>empty</i> implementation for CTP custom types validation. Should be used in most integration tests,
 * except those which explicitly depend on CTP custom types.
 */
@Component
@Primary // for most of the integration tests - don't actually validate types
public class EmptyCtpConfigStartupValidatorTestImpl implements CtpConfigStartupValidator {

    public void validateTypes() {
        // do nothing in test mode
        LoggerFactory.getLogger(this.getClass().getSimpleName()).info("Skip CTP types validation");
    }
}
