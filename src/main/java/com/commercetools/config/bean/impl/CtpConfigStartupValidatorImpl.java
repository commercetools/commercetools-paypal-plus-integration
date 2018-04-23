package com.commercetools.config.bean.impl;

import com.commercetools.config.bean.ApplicationKiller;
import com.commercetools.config.bean.CtpConfigStartupValidator;
import com.commercetools.config.ctpTypes.AggregatedCtpTypesValidationResult;
import com.commercetools.pspadapter.facade.CtpFacadeFactory;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import io.sphere.sdk.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static com.commercetools.config.constants.ExitCodes.EXIT_CODE_CTP_TYPE_INCOMPATIBLE;
import static com.commercetools.config.constants.ExitCodes.EXIT_CODE_CTP_TYPE_VALIDATION_EXCEPTION;
import static com.commercetools.config.ctpTypes.ExpectedCtpTypes.getExpectedCtpTypesFromResources;
import static com.commercetools.config.ctpTypes.TenantCtpTypesValidator.validateAndSyncCtpTypes;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Component
public class CtpConfigStartupValidatorImpl implements CtpConfigStartupValidator {

    private static final String DOCUMENTATION_REFERENCE =
            "https://github.com/commercetools/commercetools-paypal-plus-integration/blob/master/docs/MigrationGuide.md#to-v03";

    private final TenantConfigFactory tenantConfigFactory;
    private final ApplicationKiller applicationKiller;
    private final CtpFacadeFactory ctpFacadeFactory;

    @Autowired
    public CtpConfigStartupValidatorImpl(@Nonnull TenantConfigFactory tenantConfigFactory,
                                         @Nonnull CtpFacadeFactory ctpFacadeFactory,
                                         @Nonnull ApplicationKiller applicationKiller) {
        this.tenantConfigFactory = tenantConfigFactory;
        this.ctpFacadeFactory = ctpFacadeFactory;
        this.applicationKiller = applicationKiller;
    }

    @Override
    public void validateTypes() {
        try {
            AggregatedCtpTypesValidationResult typesSynchronizationResult =
                    validateAndSyncCtpTypes(ctpFacadeFactory, tenantConfigFactory.getTenantConfigs(), getExpectedCtpTypesFromResources())
                            .toCompletableFuture().join();

            processTypesSynchronizationResult(typesSynchronizationResult);

        } catch (Throwable throwable) {
            applicationKiller.killApplication(EXIT_CODE_CTP_TYPE_VALIDATION_EXCEPTION,
                    format("Exception in CTP Types validation.%nPlease refer to the documentation: %s", DOCUMENTATION_REFERENCE),
                    throwable);
        }
    }

    /**
     * Based on aggregated {@code typesProcessingResult}:<ul>
     * <li>if some error exist - show errors and finish the application</li>
     * <li>if some update actions executed - show update actions result and continue application</li>
     * <li>if neither error nor update actions found - show simple message and continue application</li>
     * </ul>
     *
     * @param typesProcessingResult result to process
     */
    private void processTypesSynchronizationResult(@Nonnull AggregatedCtpTypesValidationResult typesProcessingResult) {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        if (typesProcessingResult.hasErrorMessage()) {
            String message = format("CTP Types validation failed:%n%s%n%nPlease synchronize types manually and restart the service.%n" +
                            "Refer to the documentation: %s",
                    typesProcessingResult.getAggregatedErrorMessage(), DOCUMENTATION_REFERENCE);

            applicationKiller.killApplication(EXIT_CODE_CTP_TYPE_INCOMPATIBLE, message);
        } else if (typesProcessingResult.hasUpdatedTypes()) {
            logger.info("CTP Types updated:\n{}\n\n",
                    typesProcessingResult.getUpdatedTypes().stream()
                            .map(pair -> format("Tenant %s:Types:[%s]", pair.getKey(), pair.getValue().stream().map(Type::getKey).collect(joining(", "))))
                            .collect(joining(",\n", "[", "]")));
        } else {
            logger.info("CTP Types are up-to-date: continue application initialization\n");
        }
    }
}
