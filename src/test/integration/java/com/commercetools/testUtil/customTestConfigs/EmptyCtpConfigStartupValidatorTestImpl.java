package com.commercetools.testUtil.customTestConfigs;

import com.commercetools.config.bean.CtpConfigStartupValidator;
import com.commercetools.context.annotation.PrimaryForTest;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default <i>empty</i> implementation for CTP custom types validation. Should be used in most integration tests,
 * except those which explicitly depend on CTP custom types.
 */
@Component
@PrimaryForTest // for most of the integration tests - don't actually validate the types
public class EmptyCtpConfigStartupValidatorTestImpl implements CtpConfigStartupValidator {

    @Override
    public void validateTypes() {
        // do nothing in test mode
        LoggerFactory.getLogger(this.getClass().getSimpleName()).info("Skip CTP types validation");
    }
}
