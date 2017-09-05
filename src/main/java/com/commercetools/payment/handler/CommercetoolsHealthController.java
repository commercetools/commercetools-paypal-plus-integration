package com.commercetools.payment.handler;

import com.commercetools.pspadapter.tenant.TenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CommercetoolsHealthController extends BaseCommercetoolsController {

    private final TenantProperties tenantProperties;

    @Autowired
    public CommercetoolsHealthController(@Nonnull StringTrimmerEditor stringTrimmerEditor,
                                         @Nonnull TenantProperties tenantProperties) {
        super(stringTrimmerEditor);
        this.tenantProperties = tenantProperties;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/health",
            produces = APPLICATION_JSON_VALUE)
    public Map<String, Object> checkHealth() {
        Map<String, Object> tenantResponse = new HashMap<>();
        tenantResponse.put("tenants", this.tenantProperties.getTenants().keySet());
        tenantResponse.put("statusCode", HttpStatus.OK.value());
        return tenantResponse;
    }

}