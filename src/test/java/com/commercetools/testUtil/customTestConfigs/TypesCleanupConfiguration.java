package com.commercetools.testUtil.customTestConfigs;

import io.sphere.sdk.client.SphereClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static com.commercetools.testUtil.ctpUtil.CleanupTableUtil.*;

/**
 * <b>By importing this configuration to an <i>integration test</i> class one removes all the orders, carts, payments
 * and types from CTP project before running the tests.</b>
 * <p>
 * <b>NEVER put {@code @Configuration}, {@code @Component}, {@code @*AutoConfiguration*} and related annotations
 * to this class to avoid database cleanup on every context loading (e.g. every test class)</b>
 */
public class TypesCleanupConfiguration {

    @Autowired
    private SphereClient sphereClient;

    @PostConstruct
    void init() {
        cleanupOrders(sphereClient);
        cleanupCarts(sphereClient);
        cleanupPaymentTable(sphereClient);
        cleanupTypes(sphereClient);
    }

}
