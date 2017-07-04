package hello;

import com.commercetools.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsHandlePaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnTenantAndPayment() throws Exception {

        this.mockMvc.perform(get("/GKI/commercetools/handle/payments/XXX-YYY"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantName").value("GKI"))
                .andExpect(jsonPath("$.paymentId").value("Hello, payment [XXX-YYY]!"));
    }

    @Test
    public void finalSlashIsProcessedToo() throws Exception {

        this.mockMvc.perform(get("/asdhfasdfasf/commercetools/handle/payments/6753324-23]452-sgsfgd/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantName").value("asdhfasdfasf"))
                .andExpect(jsonPath("$.paymentId").value("Hello, payment [6753324-23]452-sgsfgd]!"));
    }


}
