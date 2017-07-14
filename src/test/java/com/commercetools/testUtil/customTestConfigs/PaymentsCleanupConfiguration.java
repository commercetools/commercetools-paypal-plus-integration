package com.commercetools.testUtil.customTestConfigs;

import io.sphere.sdk.client.SphereClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupPaymentTable;

/**
 * <b>By importing this configuration to an <i>integration test</i> class one removes all the payments from CTP project
 * before running the tests.
 */
public class PaymentsCleanupConfiguration {

    @Autowired
    private SphereClient sphereClient;

    @PostConstruct
    void init() {
        cleanupPaymentTable(sphereClient);
    }


}