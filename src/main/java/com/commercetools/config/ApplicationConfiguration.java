package com.commercetools.config;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.impl.PaymentMapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Bean
    @Autowired
    public PaymentMapper paymentMapper(PaypalPlusFormatter paypalPlusFormatter) {
        return new PaymentMapperImpl(paypalPlusFormatter);
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
}
