package com.commercetools.config;

import com.commercetools.helper.formatter.PaypalPlusFormatter;
import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.helper.mapper.PaymentMapper;
import com.commercetools.helper.mapper.impl.PaymentMapperImpl;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nonnull;

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

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .disableHtmlEscaping()
                .create();
    }

    /**
     * Use {@link #gson()} as a default JSON HTTP request/response mapper.
     * <p>
     * <b>Note:</b> this mapping is important at least for {@link PaymentHandleResponse#getPayment()} method. See
     * respective documentation for details.
     *
     * @param gson default application JSON mapper.
     * @return default HTTP request/response mapper, based on {@link #gson()} bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public GsonHttpMessageConverter gsonHttpMessageConverter(@Nonnull Gson gson) {
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gson);
        return converter;
    }
}
