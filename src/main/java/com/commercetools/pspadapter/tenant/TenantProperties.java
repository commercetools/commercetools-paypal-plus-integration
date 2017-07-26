package com.commercetools.pspadapter.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Configuration
@ConfigurationProperties("tenantConfig")
@Validated
public class TenantProperties {

    @Valid
    private List<Tenant> tenants;

    public List<Tenant> getTenants() {
        return tenants;
    }

    public void setTenants(List<Tenant> tenants) {
        this.tenants = tenants;
    }

    public static class Tenant {

        @NotNull
        private String name;

        @Valid
        @NotNull
        private Ctp ctp;

        @Valid
        @NotNull
        private PaypalPlus paypalPlus;

        @ConfigurationProperties("ctp")
        public static class Ctp {

            @NotNull
            private String projectKey;

            @NotNull
            private String clientId;

            @NotNull
            private String clientSecret;

            public String getProjectKey() {
                return projectKey;
            }

            public void setProjectKey(String projectKey) {
                this.projectKey = projectKey;
            }

            public String getClientId() {
                return clientId;
            }

            public void setClientId(String clientId) {
                this.clientId = clientId;
            }

            public String getClientSecret() {
                return clientSecret;
            }

            public void setClientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
            }
        }

        @ConfigurationProperties("paypalPlus")
        public static class PaypalPlus {

            @NotNull
            private String id;

            @NotNull
            private String secret;

            @NotNull
            private String mode;
            
            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public String getMode() {
                return mode;
            }

            public void setMode(String mode) {
                this.mode = mode;
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Ctp getCtp() {
            return ctp;
        }

        public void setCtp(Ctp ctp) {
            this.ctp = ctp;
        }

        public PaypalPlus getPaypalPlus() {
            return paypalPlus;
        }

        public void setPaypalPlus(PaypalPlus paypalPlus) {
            this.paypalPlus = paypalPlus;
        }
    }
}