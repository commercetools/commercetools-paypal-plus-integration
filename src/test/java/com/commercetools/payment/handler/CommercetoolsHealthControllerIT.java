package com.commercetools.payment.handler;

import com.commercetools.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.commercetools.testUtil.TestConstants.MAIN_TEST_TENANT_NAME;
import static com.commercetools.testUtil.TestConstants.SECOND_TEST_TENANT_NAME;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class CommercetoolsHealthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void checkHealth() throws Exception {
        // get
        assertResponse(mockMvc.perform(get("/")));
        assertResponse(mockMvc.perform(get("/health")));
        assertResponse(mockMvc.perform(get("/health/")));

        // post
        assertResponse(mockMvc.perform(post("/")));
        assertResponse(mockMvc.perform(post("/health")));
        assertResponse(mockMvc.perform(post("/health/")));
    }

    private void assertResponse(ResultActions perform) throws Exception {
        perform
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.tenants").value(containsInAnyOrder(MAIN_TEST_TENANT_NAME, SECOND_TEST_TENANT_NAME)))
                .andExpect(jsonPath("$.applicationInfo.version").value("undefined"))
                .andExpect(jsonPath("$.applicationInfo.title").value("undefined"));
    }
}