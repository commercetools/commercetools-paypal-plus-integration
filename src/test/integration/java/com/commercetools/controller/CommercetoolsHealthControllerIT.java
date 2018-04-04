package com.commercetools.controller;

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
import static org.assertj.core.api.Assertions.assertThat;
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
        assertMinifiedResponse(mockMvc.perform(get("")));
        assertMinifiedResponse(mockMvc.perform(get("/")));
        assertMinifiedResponse(mockMvc.perform(get("/health")));
        assertMinifiedResponse(mockMvc.perform(get("/health/")));

        // post
        assertMinifiedResponse(mockMvc.perform(post("")));
        assertMinifiedResponse(mockMvc.perform(post("/")));
        assertMinifiedResponse(mockMvc.perform(post("/health")));
        assertMinifiedResponse(mockMvc.perform(post("/health/")));
    }

    @Test
    public void checkHealth_prettyPrint() throws Exception {
        // get
        assertPrettyResponse(mockMvc.perform(get("").param("pretty", "")));
        assertPrettyResponse(mockMvc.perform(get("").param("pretty", "foo")));
        assertPrettyResponse(mockMvc.perform(get("/").param("pretty", "")));
        assertPrettyResponse(mockMvc.perform(get("/health").param("pretty", "foo")));
        assertPrettyResponse(mockMvc.perform(get("/health/").param("pretty", "")));
        assertPrettyResponse(mockMvc.perform(get("/health/").param("pretty", "foo")));

        // post
        assertPrettyResponse(mockMvc.perform(post("").param("pretty", "")));
        assertPrettyResponse(mockMvc.perform(post("").param("pretty", "foo")));
        assertPrettyResponse(mockMvc.perform(post("/").param("pretty", "")));
        assertPrettyResponse(mockMvc.perform(post("/health").param("pretty", "foo")));
        assertPrettyResponse(mockMvc.perform(post("/health/").param("pretty", "")));
        assertPrettyResponse(mockMvc.perform(post("/health/").param("pretty", "foo")));
    }

    private void assertMinifiedResponse(ResultActions perform) throws Exception {
        assertResponseJson(perform);
        assertThat(perform.andReturn().getResponse().getContentAsString()).hasLineCount(1);
    }

    private void assertPrettyResponse(ResultActions perform) throws Exception {
        assertResponseJson(perform);

        final String contentAsString = perform.andReturn().getResponse().getContentAsString();

        // at least one line for tenants, applicationInfo, version and title + 2 lines for root {}
        assertThat(contentAsString.split("[\n\r]+").length).isGreaterThanOrEqualTo(6);

        // first level keys should be indented at least 1 space
        assertThat(contentAsString).containsPattern("\\s+\"tenants\"");
        assertThat(contentAsString).containsPattern("\\s+\"applicationInfo\"");

        //second level "version" and "title" should have at least 2 leading spaces
        assertThat(contentAsString).containsPattern("\\s{2,}\"version\"");
        assertThat(contentAsString).containsPattern("\\s{2,}\"title\"");
    }

    private void assertResponseJson(ResultActions perform) throws Exception {
        perform
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.tenants").value(containsInAnyOrder(MAIN_TEST_TENANT_NAME, SECOND_TEST_TENANT_NAME)))
                .andExpect(jsonPath("$.applicationInfo.version").value("undefined"))
                .andExpect(jsonPath("$.applicationInfo.title").value("undefined"));
    }
}