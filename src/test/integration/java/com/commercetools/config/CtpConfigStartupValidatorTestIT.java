package com.commercetools.config;

import com.commercetools.config.bean.ApplicationKiller;
import com.commercetools.config.bean.CtpConfigStartupValidator;
import com.commercetools.config.bean.impl.CtpConfigStartupValidatorImpl;
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
import org.springframework.beans.factory.annotation.Qualifier;
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
import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupAllTenantsTypes;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test {@link CtpConfigStartupValidatorImpl} with different initial CTP project config.
 * <p>
 * <b>Note:</b>
 * <ul>
 * <li>since {@link CtpConfigStartupValidator} bean is overridden by
 * {@link com.commercetools.testUtil.customTestConfigs.EmptyCtpConfigStartupValidatorTestImpl EmptyCtpConfigStartupValidatorTestImpl}
 * for most of the test - we use explicit qualified bean name {@code ctpConfigStartupValidatorImpl} for this test</li>
 * <li>before and after each test case we clean all the custom types</li>
 * <li>to make actual tests against different cases we "mock" CTP types state from the test resources
 * (see {@link #setupTypesFromResources(java.lang.String)}) and then try to verify/update them directly calling
 * {@link CtpConfigStartupValidator#validateTypes()}</li>
 * </ul>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
public class CtpConfigStartupValidatorTestIT {

    @Autowired
    @Qualifier("ctpConfigStartupValidatorImpl") // we need to test real CtpConfigStartupValidatorImpl here, not injected mock
    private CtpConfigStartupValidator ctpConfigStartupValidator;

    @Autowired
    protected TenantConfigFactory tenantConfigFactory;

    @Autowired
    protected SphereClientFactory sphereClientFactory;

    @Autowired
    protected CtpFacadeFactory ctpFacadeFactory;

    // don't really kill the app - just verify the mock is called when expected
    @MockBean
    protected ApplicationKiller applicationKiller;


    // paths to resources with mocking "actual" CTP project Type configuration
    private static final String RECOVERABLE_CTP_TYPES_MOCKS_DIR = "ctp/typesTestMocks/recoverableTypes/";
    private static final String RECOVERABLE_CTP_TYPES_WITH_ENUMS_MOCKS_DIR = "ctp/typesTestMocks/recoverableTypesWithEnums/";
    private static final String UNRECOVERABLE_CTP_TYPES_MOCKS_DIR = "ctp/typesTestMocks/unrecoverableTypes/";

    @Before
    public void setUp() throws Exception {
        assertThat(tenantConfigFactory).isNotNull();
        assertThat(tenantConfigFactory.getTenantConfigs().size()).isGreaterThanOrEqualTo(1); // ensure we test at least some tenants

        cleanupAllTenantsTypes(tenantConfigFactory, sphereClientFactory);
    }

    @After
    public void tearDown() throws Exception {
        cleanupAllTenantsTypes(tenantConfigFactory, sphereClientFactory);
    }

    /**
     * Verifies all types are created from scratch
     */
    @Test
    public void whenCtpProjectIsEmpty_allTheTypesAreCreatedFromScratch() throws Exception {
        ctpConfigStartupValidator.validateTypes();

        verifyTenantsTypesAreCreated();
    }

    /**
     * If the types have incompatible properties - the application is killed with logging message
     */
    @Test
    public void whenCtpProjectHasUnrecoverableValues_theApplicationIsKilledWithLoggingMessage() throws Exception {
        setupTypesFromResources(UNRECOVERABLE_CTP_TYPES_MOCKS_DIR);

        ctpConfigStartupValidator.validateTypes();

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

    /**
     * Verifies types are updated from some initial state and at the end they have expected properties.
     */
    @Test
    public void whenCtpProjectHasRecoverableValues_allTheTypesAreUpdated() throws Exception {
        setupTypesFromResources(RECOVERABLE_CTP_TYPES_MOCKS_DIR);

        ctpConfigStartupValidator.validateTypes();

        verifyTenantsTypesAreCreated();
    }

    @Test
    public void whenCtpProjectHasRecoverableValuesWithMissedEnum_allTheTypesAreUpdated() throws Exception {
        setupTypesFromResources(RECOVERABLE_CTP_TYPES_WITH_ENUMS_MOCKS_DIR);

        ctpConfigStartupValidator.validateTypes();

        verifyTenantsTypesAreCreated();
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
