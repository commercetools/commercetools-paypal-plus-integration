package com.commercetools.controller;

import com.commercetools.http.converter.json.PrettyFormattedBody;
import com.commercetools.model.ApplicationInfo;
import com.commercetools.payment.handler.BaseCommercetoolsController;
import com.commercetools.pspadapter.tenant.TenantProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static com.commercetools.model.ApplicationInfo.APP_INFO_KEY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Health endpoint, which might be used to check the service availability.
 * <p>
 * The endpoint accepts both <i>GET</i> and <i>POST</i> requests, both to <i>/health</i> and <i>/</i> (root) endpoints.
 * <p>
 * The response is always <i>200 (OK)</i>, JSON body contains:<ul>
 * <li>{@code tenants}: list of tenants this services processes</li>
 * <li>{@code applicationInfo}: {@link ApplicationInfo} about the running application</li>
 * </ul>
 */
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
            value = {"/health", "/"},
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkHealth(@RequestParam(required = false) String pretty,
                                         @Autowired ApplicationInfo applicationInfo) {
        Map<String, Object> tenantResponse = new HashMap<>();
        tenantResponse.put("tenants", this.tenantProperties.getTenants().keySet());
        tenantResponse.put(APP_INFO_KEY, applicationInfo);

        return new ResponseEntity<>(PrettyFormattedBody.of(tenantResponse, pretty != null),
                HttpStatus.OK);
    }
}