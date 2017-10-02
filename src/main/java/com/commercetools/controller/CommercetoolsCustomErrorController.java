package com.commercetools.controller;

import com.commercetools.model.ApplicationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Map;

import static com.commercetools.model.ApplicationInfo.APP_INFO_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Custom (unexpected) error response implementation.
 * <p>
 * The implementation returns error response with JSON body content with the following properties:<ul>
 * <li>{@code requestMethod}: used request method which landed to the error (<i>POST</i>, <i>GET</i> and so on)</li>
 * <li>{@code timestamp}: an ISO-8601 representation of the request time</li>
 * <li>{@code applicationInfo}: {@link ApplicationInfo} about the running application</li>
 * <li>other default values from {@link ErrorAttributes} (like <i>status</i>, <i>error</i>, <i>message</i>,
 * <i>path</i>)</li>
 * </ul>
 */
@RestController
public class CommercetoolsCustomErrorController implements ErrorController {

    private static final String PATH = "/error";

    private final ErrorAttributes errorAttributes;

    @Autowired
    public CommercetoolsCustomErrorController(@Nonnull ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = PATH, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> error(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                   @Nonnull ApplicationInfo applicationInfo) {
        return new ResponseEntity<>(getErrorAttributes(request, applicationInfo), HttpStatus.valueOf(response.getStatus()));
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }

    private Map<String, Object> getErrorAttributes(@Nonnull HttpServletRequest request,
                                                   @Nonnull ApplicationInfo applicationInfo) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> errorAttributes = this.errorAttributes.getErrorAttributes(requestAttributes, false);
        errorAttributes.put("requestMethod", request.getMethod());

        // override timestamp from DefaultErrorAttributes.getErrorAttributes() which is too verbose
        errorAttributes.put("timestamp", Instant.now().toString());
        errorAttributes.put(APP_INFO_KEY, applicationInfo);
        return errorAttributes;
    }

}