package com.commercetools.config;

import com.commercetools.config.bean.ApplicationKiller;
import com.commercetools.config.ctpTypes.AggregatedCtpTypesValidationResult;
import com.commercetools.config.ctpTypes.TenantCtpTypesValidationAction;
import com.commercetools.config.ctpTypes.TenantCtpTypesValidator;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import io.sphere.sdk.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.commercetools.config.constants.ExitCodes.EXIT_CODE_CTP_TYPE_INCOMPATIBLE;
import static com.commercetools.config.constants.ExitCodes.EXIT_CODE_CTP_TYPE_VALIDATION_EXCEPTION;
import static com.commercetools.config.ctpTypes.ExpectedCtpTypes.getExpectedCtpTypesFromResources;
import static java.lang.String.format;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Verify CTP project settings are correct for all configured tenants.
 */
public class CtpStartupConfiguration {

    private final TenantConfigFactory tenantConfigFactory;
    private final ApplicationKiller applicationKiller;

    @Autowired
    public CtpStartupConfiguration(@Nonnull TenantConfigFactory tenantConfigFactory,
                                   @Nonnull ApplicationKiller applicationKiller) {
        this.tenantConfigFactory = tenantConfigFactory;
        this.applicationKiller = applicationKiller;
    }

    /**
     * Verify the CTP {@link Type}s are configured like expected. The implementation compares actual tenant
     * {@link Type}s with expected types from the embed resources
     * (see {@link com.commercetools.config.ctpTypes.ExpectedCtpTypes}). If the types could be updated -
     * the update actions are performed. Otherwise error message is show and application is finished with error code.
     * <p>
     * If neither errors exist nor update actions required - continue application startup.
     */
    @PostConstruct
    void validateTypes() {
        try {

            AggregatedCtpTypesValidationResult typesSynchronizationResult = getAggregatedCtpTypesValidationResult();
            processTypesSynchronizationResult(typesSynchronizationResult);

        } catch (Throwable throwable) {
            applicationKiller.killApplication(EXIT_CODE_CTP_TYPE_VALIDATION_EXCEPTION,
                    "Exception in CTP Types validation", throwable);
        }
    }

    /**
     * For every tenant get a list of errors or update actions and execute them. Return aggregated result.
     *
     * @return {@link AggregatedCtpTypesValidationResult} with types validation/synchronization results for all tenants.
     */
    private AggregatedCtpTypesValidationResult getAggregatedCtpTypesValidationResult() {
        Set<Type> expectedTypesSet = getExpectedCtpTypesFromResources();

        // 1. fetch and validate expected types from all the tenants
        List<CompletableFuture<TenantCtpTypesValidationAction>> tenantsValidatorsStage =
                tenantConfigFactory.getTenantConfigs().parallelStream()
                        .map(tenantConfig -> TenantCtpTypesValidator.validateTenantTypes(tenantConfig.getTenantName(),
                                new CtpFacadeFactory(tenantConfig).getCtpFacade().getTypeService(),
                                expectedTypesSet))
                        .map(CompletionStage::toCompletableFuture)
                        .collect(toList());

        // 2. process/aggregate validation results
        return allOf(tenantsValidatorsStage.toArray(new CompletableFuture[]{}))
                .thenApply(voidValue -> tenantsValidatorsStage.stream().map(CompletableFuture::join).collect(toList()))
                .thenCompose(AggregatedCtpTypesValidationResult::executeAndAggregateTenantValidationResults)
                .join();
    }

    /**
     * Based on aggregated {@code typesProcessingResult}:<ul>
     * <li>if some error exist - show errors and finish the application</li>
     * <li>if some update actions executed - show update actions result and continue application</li>
     * <li>if neither error nor update actions found - show simple message and continue application</li>
     * </ul>
     *
     * @param typesProcessingResult
     */
    private void processTypesSynchronizationResult(@Nonnull AggregatedCtpTypesValidationResult typesProcessingResult) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        if (typesProcessingResult.hasErrorMessage()) {
            String message = format("CTP Types validation failed:%n%s%n%nPlease, synchronize types manually and restart the service",
                    typesProcessingResult.getAggregatedErrorMessage());

            applicationKiller.killApplication(EXIT_CODE_CTP_TYPE_INCOMPATIBLE, message);
        }

        if (typesProcessingResult.hasUpdatedTypes()) {
            logger.info("CTP Types updated:\n{}\n\n",
                    typesProcessingResult.getUpdatedTypes().stream()
                            .map(pair -> format("Tenant %s:Types:[%s]", pair.getKey(), pair.getValue().stream().map(Type::getKey).collect(joining(", "))))
                            .collect(joining(",\n", "[", "]")));
        } else {
            logger.info("CTP Types are up-to-date: continue application initialization\n");
        }
    }
}
