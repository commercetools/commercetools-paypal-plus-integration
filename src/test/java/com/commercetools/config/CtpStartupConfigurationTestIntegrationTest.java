package com.commercetools.config;

import com.commercetools.config.bean.ApplicationKiller;
import com.commercetools.config.ctpTypes.ExpectedCtpTypes;
import com.commercetools.config.ctpTypes.TenantCtpTypesValidator;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.facade.SphereClientFactory;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.service.ctp.TypeService;
import com.commercetools.testUtil.CompletionStageUtil;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.TypeDraft;
import io.sphere.sdk.types.TypeDraftBuilder;
import io.sphere.sdk.types.commands.TypeCreateCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static com.commercetools.config.constants.ExitCodes.EXIT_CODE_CTP_TYPE_INCOMPATIBLE;
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test {@link CtpStartupConfiguration} with different initial CTP project config.
 * <p>
 * <b>Note:</b> since {@link CtpStartupConfiguration} is set by default to {@link com.commercetools.Application}
 * configuration - it will start any on this test class initialization, e.g. it will try to synchronize the types.
 * This causes two issues:<ul>
 * <li>If current CTP integration test projects are "unrecoverable" (e.g. contain types which can not be updated)
 * - the test will always fail. In this case just clean up all they types on the integration test CTP projects.</li>
 * <li>To make actual tests we "mock" CTP types state from the test resources
 * (see {@link #setupTypesFromResources(java.lang.String)}) and then try to verify/update them directly calling
 * {@link CtpStartupConfiguration#validateTypes()}</li>
 * </ul>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CtpStartupConfigurationTestIntegrationTest {

    @Autowired
    private CtpStartupConfiguration ctpStartupConfiguration;

    @Autowired
    protected TenantConfigFactory tenantConfigFactory;

    @Autowired
    protected SphereClientFactory sphereClientFactory;

    @Autowired
    protected CtpFacadeFactory ctpFacadeFactory;

    // don't really kill the app - just verify the mock is called when expected
    @MockBean
    protected ApplicationKiller applicationKiller;


    private static final String RECOVERABLE_CTP_TYPES_MOCKS_DIR = "ctp/typesTestMocks/recoverableTypes/";
    private static final String UNRECOVERABLE_CTP_TYPES_MOCKS_DIR = "ctp/typesTestMocks/unrecoverableTypes/";

    @Before
    public void setUp() throws Exception {
        assertThat(tenantConfigFactory).isNotNull();
        assertThat(tenantConfigFactory.getTenantConfigs().size()).isGreaterThanOrEqualTo(1); // ensure we test at least some tenants

        wipeOutTypes();
    }

    @After
    public void tearDown() throws Exception {
        wipeOutTypes();
    }

    /**
     * Verifies all types are created from scratch
     */
    @Test
    public void whenCtpProjectIsEmpty_allTheTypesAreCreatedFromScratch() throws Exception {
        ctpStartupConfiguration.validateTypes();

        verifyTenantsTypesAreCreated();
    }

    /**
     * Verifies types are updated from some initial state and at the end they have expected properties.
     */
    @Test
    public void whenCtpProjectHasRecoverableValues_allTheTypesAreUpdated() throws Exception {
        setupTypesFromResources(RECOVERABLE_CTP_TYPES_MOCKS_DIR);

        ctpStartupConfiguration.validateTypes();

        verifyTenantsTypesAreCreated();
    }

    /**
     * If the types have incompatible properties - the application is killed with logging message
     */
    @Test
    public void whenCtpProjectHasUnrecoverableValues_theApplicationIsKilledWithLoggingMessage() throws Exception {
        setupTypesFromResources(UNRECOVERABLE_CTP_TYPES_MOCKS_DIR);

        ctpStartupConfiguration.validateTypes();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(applicationKiller, times(1))
                .killApplication(eq(EXIT_CODE_CTP_TYPE_INCOMPATIBLE), messageCaptor.capture());

        assertThat(messageCaptor.getValue()).contains("CTP Types validation failed",
                "payment-paypal",
                "paypal-plus-interaction-notification",
                "languageCode",
                "amount");

        assertThat(messageCaptor.getValue())
                .doesNotContain("paypal-plus-interaction-request")
                .doesNotContain("paypal-plus-interaction-response");
    }

    private void wipeOutTypes() {
        tenantConfigFactory.getTenantConfigs().parallelStream()
                .map(sphereClientFactory::createSphereClient)
                .forEach(sphereClient -> {
                    cleanupOrders(sphereClient);
                    cleanupCarts(sphereClient);
                    cleanupPaymentTable(sphereClient);
                    cleanupTypes(sphereClient);
                });
    }

    /**
     * Verifies all the types in every {@link TenantConfigFactory#getTenantConfigs()} contain expected fields like in
     * {@link ExpectedCtpTypes}
     */
    protected void verifyTenantsTypesAreCreated() throws Exception {
        tenantConfigFactory.getTenantConfigs().parallelStream()
                .map(tenantConfig -> ctpFacadeFactory.getCtpFacade(tenantConfig).getTypeService())
                .map(TypeService::getTypes)
                .map(CompletionStage::toCompletableFuture)
                .map(CompletableFuture::join)
                .forEach(this::verifyTenantTypes);
    }

    /**
     * Verify types for each tenant have the same configuration, like in {@link ExpectedCtpTypes}
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

    /**
     * Prepare CTP project before tests:<ol>
     * <li>remove all the types</li>
     * <li>create types according to mocks in {@code resourceDirPath}</li>
     * </ol>
     *
     * @param resourceDirPath test resources directory with {@link Type} mocks to create. The directory should contain
     *                        files in the same formats like in {@link ExpectedCtpTypes#PATH_TO_TYPE_MOCKS}
     */
    protected void setupTypesFromResources(String resourceDirPath) throws URISyntaxException {
        File ctpRecoverableMockTypesDir = new File(this.getClass().getClassLoader().getResource(resourceDirPath).toURI());

        File[] typeFiles = ctpRecoverableMockTypesDir.listFiles(fileName -> fileName.getName().endsWith(".json"));
        assertThat(typeFiles).isNotEmpty();
        List<TypeDraft> recoverableTypeDrafts = Stream.of(typeFiles)
                .map(file -> SphereJsonUtils.readObjectFromResource(resourceDirPath + file.getName(), Type.class))
                .map(type -> TypeDraftBuilder.of(type.getKey(), type.getName(),
                        type.getResourceTypeIds())
                        .fieldDefinitions(type.getFieldDefinitions())
                        .description(type.getDescription())
                        .build())
                .collect(toList());

        tenantConfigFactory.getTenantConfigs()
                .parallelStream()
                .flatMap(config -> recoverableTypeDrafts.parallelStream()
                        .map(draft -> sphereClientFactory.createSphereClient(config).execute(TypeCreateCommand.of(draft))))
                .forEach(CompletionStageUtil::executeBlocking);
    }
}