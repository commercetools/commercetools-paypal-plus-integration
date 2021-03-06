package com.commercetools.pspadapter.notification.validation;

import com.commercetools.payment.notification.CommercetoolsPaymentNotificationController;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.commercetools.payment.constants.Psp.NOTIFICATION_PATH_URL;

/**
 * This filter enables multiple reads from request body that comes to
 * the Paypal Plus notification endpoint. It's necessary because
 * {@link NotificationValidationInterceptor} is used to check
 * if the whole request is valid and then {@link CommercetoolsPaymentNotificationController}
 * is used to process the request body
 */
public class NotificationValidationFilter extends GenericFilterBean {

    private static final String REGEX_URL_PATTERN = "regexUrlPattern";

    private Pattern pattern;

    private String urlPatternString = "/.*/" + NOTIFICATION_PATH_URL;

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();
        FilterConfig filterConfig = getFilterConfig();
        if (filterConfig != null) {
            String urlPatternString = filterConfig.getInitParameter(REGEX_URL_PATTERN);
            if (urlPatternString != null) {
                this.urlPatternString = urlPatternString;
            }
        }
        pattern = Pattern.compile(this.urlPatternString);
    }

    @Override
    public void doFilter(@Nonnull ServletRequest request,
                         @Nonnull ServletResponse response,
                         @Nonnull FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest currentRequest = (HttpServletRequest) request;
            Matcher m = pattern.matcher(currentRequest.getServletPath());
            if (m.matches()) {
                // if request is notification, then we use multiple read wrapper
                ServletRequest wrappedRequest = new MultipleReadServletRequest(currentRequest);
                chain.doFilter(wrappedRequest, response);
            } else {
                // if not, then just pass the original request and response
                chain.doFilter(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}