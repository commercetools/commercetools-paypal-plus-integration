package com.commercetools.test.autoconfigure.web.servlet;

import com.commercetools.test.web.servlet.MockMvcAsync;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Auto-configuration for {@link MockMvcAsync}.
 * <p>
 * <b>Note:</b> if you know how to exclude the configuration from non-MockMvc tests - do this,
 * i've not found better solutions besides mark {@link #asyncMockMvc(MockMvc)} as {@link Lazy} so the bean is not
 * initialized for simple tests without MockMvc (but the configuration is still created for all the tests).
 */
@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(MockMvcAutoConfiguration.class)
public class AsyncMockMvcAutoConfiguration {

    @Bean
    @Lazy // avoid bean initialization if MockMvc is not injected to current test suit
    public MockMvcAsync asyncMockMvc(MockMvc mockMvc) {
        return new MockMvcAsync(mockMvc);
    }
}
