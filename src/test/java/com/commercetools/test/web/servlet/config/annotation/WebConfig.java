package com.commercetools.test.web.servlet.config.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    /**
     * Default timeout for async operations in the tests. This is useful for operations from
     * {@link com.commercetools.test.web.servlet.MockMvcAsync}
     *
     * @param configurer {@link AsyncSupportConfigurer} where to set properties.
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(60_000);
        super.configureAsyncSupport(configurer);
    }
}
