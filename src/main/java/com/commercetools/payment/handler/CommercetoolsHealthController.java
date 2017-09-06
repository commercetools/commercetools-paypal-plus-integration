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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

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
    public CompletionStage<Map<String, Object>> checkHealth() {

        CompletableFuture<Map<String, Object>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Map<String, Object> tenantResponse = new HashMap<>();
            tenantResponse.put("tenants", this.tenantProperties.getTenants().keySet());
            tenantResponse.put("statusCode", HttpStatus.OK.value());
            return tenantResponse;
        });

        return mapCompletableFuture;
    }

}