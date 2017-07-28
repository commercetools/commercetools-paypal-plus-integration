package com.commercetools.payment.handler;

import com.commercetools.Application;
import com.commercetools.pspadapter.tenant.TenantProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsCreatePaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantProperties tenantProperties;

    private String existingProjectName;

    @Before
    public void setUp() {
        this.existingProjectName = tenantProperties.getTenants().keySet().iterator().next();
    }

    @Test
    public void shouldReturnTenantAndPayment() throws Exception {
        this.mockMvc.perform(get("/" + this.existingProjectName + "/commercetools/create/payments/XXX-YYY"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void finalSlashIsProcessedToo() throws Exception {
        this.mockMvc.perform(get("/asdhfasdfasf/commercetools/create/payments/6753324-23452-sgsfgd/"))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }


}
