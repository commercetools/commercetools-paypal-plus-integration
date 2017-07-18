package com.commercetools.testUtil.customTestConfigs;

import io.sphere.sdk.client.SphereClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.cleanupCarts;

/**
 * <b>By importing this configuration to an <i>integration test</i> class one removes all the payments from CTP project
 * before running the tests.</b>
 * <p>
 * <b>NEVER put {@code @Configuration}, {@code @Component}, {@code @*AutoConfiguration*} and related
 * annotations to this class to avoid database cleanup on every context loading (e.g. every test class)</b>
 */
public class CartsCleanupConfiguration {

    @Autowired
    private SphereClient sphereClient;

    @PostConstruct
    void init() {
        cleanupCarts(sphereClient);
    }

}