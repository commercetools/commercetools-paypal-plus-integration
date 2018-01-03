package com.commercetools.config;

import com.commercetools.config.ctpTypes.ExpectedCtpTypes;
import com.commercetools.config.ctpTypes.TenantCtpTypesValidator;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.ctp.TypeService;
import io.sphere.sdk.types.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test just starts the application and verifies that after successful startup the types defined in
 * {@link ExpectedCtpTypes} and the actual tenant CTP projects are the same.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration
public class CtpStartupConfigurationTest_onDefaultStartup {

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    @Before
    public void setUp() throws Exception {
        assertThat(tenantConfigFactory).isNotNull();
        assertThat(tenantConfigFactory.getTenantConfigs().size()).isGreaterThanOrEqualTo(1); // ensure we test at least some tenants
    }

    @Test
    public void verifyTheTypesAreCreated() throws Exception {
        tenantConfigFactory.getTenantConfigs().parallelStream()
                .map(tenantConfig -> new CtpFacadeFactory(tenantConfig).getCtpFacade().getTypeService())
                .map(TypeService::getTypes)
                .map(CompletionStage::toCompletableFuture)
                .map(CompletableFuture::join)
                .forEach(this::verifyTenantTypes);
    }

    /**
     * Verify types for each tenant.
     */
    private void verifyTenantTypes(List<Type> types) {
        Map<String, Type> newTenantTypes = types.stream().collect(toMap(Type::getKey, i -> i));
        ExpectedCtpTypes.getExpectedCtpTypesFromResources()
                .forEach(expectedType -> {
                    Type actualType = newTenantTypes.get(expectedType.getKey());
                    assertThat(actualType).isNotNull();
                    assertThat(actualType.getResourceTypeIds()).containsAll(expectedType.getResourceTypeIds());
                    expectedType.getFieldDefinitions()
                            .forEach(expectedField -> {

                                TenantCtpTypesValidator.FieldDefinitionTuple fieldDefinitionTuple =
                                        TenantCtpTypesValidator.FieldDefinitionTuple.of(expectedField, expectedType.getFieldDefinitionByName(expectedField.getName()));
                                assertThat(fieldDefinitionTuple).isNotNull();
                                assertThat(fieldDefinitionTuple.areDifferent()).isFalse();
                            });
                });
    }
}