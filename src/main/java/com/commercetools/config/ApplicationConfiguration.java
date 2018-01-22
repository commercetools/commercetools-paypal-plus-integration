package com.commercetools.config;

import com.commercetools.config.bean.ApplicationKiller;
import com.commercetools.config.bean.impl.ApplicationKillerImpl;
import com.commercetools.http.converter.json.PrettyGsonMessageConverter;
import com.commercetools.pspadapter.paymentHandler.impl.PaymentHandleResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class ApplicationConfiguration {

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
     * @return same as {@link #gson()}, but with <code>{@link Gson#prettyPrinting} == true</code>, e.g. use indentation
     */
    @Bean
    public Gson prettyGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    /**
     * Custom JSON objects mapper: uses {@link #gson()} as a default JSON HTTP request/response mapper
     * and {@link #prettyGson()} as mapper for pretty-printed JSON objects. See {@link PrettyGsonMessageConverter} for
     * how pretty print is requested.
     * <p>
     * <b>Note:</b> {@link FieldNamingPolicy#IDENTITY} field mapping policy is important at least for
     * {@link PaymentHandleResponse#getPayment()} method. See respective documentation for details.
     *
     * @return default HTTP request/response mapper, based on {@link #gson()} bean.
     */
    @Bean
    public GsonHttpMessageConverter gsonMessageConverter() {
        return new PrettyGsonMessageConverter(gson(), prettyGson());
    }

    @Bean
    public ApplicationKiller applicationKiller() {
        return new ApplicationKillerImpl();
    }
}
