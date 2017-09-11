package com.commercetools.config;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public PaypalPlusFormatter paypalPlusFormatter() {
        return new PaypalPlusFormatterImpl();
    }

    /**
     * @return bean which forces to treat trailing slash same as without it.
     */
    @Bean
    public RequestMappingHandlerMapping useTrailingSlash() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = new RequestMappingHandlerMapping();
        requestMappingHandlerMapping.setUseTrailingSlashMatch(true);
        return requestMappingHandlerMapping;
    }

    /**
     * @return bean which allows to trim whitespaces in URL request arguments (path variables, request params)
     */
    @Bean
    public StringTrimmerEditor stringTrimmerEditor() {
        return new StringTrimmerEditor(false);
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .disableHtmlEscaping()
                .create();
    }
}
