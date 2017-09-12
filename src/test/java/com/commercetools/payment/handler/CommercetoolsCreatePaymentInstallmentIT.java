package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.pspadapter.facade.CtpFacade;
import com.commercetools.pspadapter.tenant.TenantConfig;
import com.commercetools.pspadapter.tenant.TenantConfigFactory;
import com.commercetools.test.web.servlet.MockMvcAsync;
import com.commercetools.testUtil.customTestConfigs.OrdersCartsPaymentsCleanupConfiguration;
import io.sphere.sdk.client.SphereClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@Import(OrdersCartsPaymentsCleanupConfiguration.class)
// completely wipe-out CTP project Payment, Cart, Order endpoints before the test cases
public class CommercetoolsCreatePaymentInstallmentIT {

    @Autowired
    private MockMvcAsync mockMvcAsync;

    @Autowired
    private TenantConfigFactory tenantConfigFactory;

    private TenantConfig tenantConfig;
    private SphereClient sphereClient;
    private CtpFacade ctpFacade;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void installmentPaymentCreated() throws Exception {
        // TODO: WIP: will be supplied as a separate PR

    }
}
