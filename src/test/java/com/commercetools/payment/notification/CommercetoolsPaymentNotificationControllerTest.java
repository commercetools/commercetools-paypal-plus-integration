package com.commercetools.payment.notification;

import com.commercetools.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsPaymentNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void handlePaymentsReturnsTenantAndRequestParameters() throws Exception {
        this.mockMvc.perform(post("/56654vbv/paypalplus/notification")
                        .param("ggg", "ppp")
                        .param("ttt", "345")
                        .param("bool", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("56654vbv")))
                .andExpect(content().string(containsString("ggg=ppp")))
                .andExpect(content().string(containsString("ttt=345")))
                .andExpect(content().string(containsString("bool=true")));
    }

    @Test
    public void handlePaymentsIgnoresTrailingSlash() throws Exception {
        this.mockMvc.perform(post("/blah-blah/paypalplus/notification")
                .param("xxx", "yyy"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("blah-blah")))
                .andExpect(content().string(containsString("xxx=yyy")));
    }
}