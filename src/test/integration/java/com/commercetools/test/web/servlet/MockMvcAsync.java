package com.commercetools.test.web.servlet;

import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * Kind of "extend" default {@link MockMvc} (final) functionality for async (e.g. {@link java.util.concurrent.CompletionStage})
 * operations, which requires a bit different workflow.
 */
public final class MockMvcAsync {
    private final MockMvc mockMvc;

    public MockMvcAsync(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    /**
     * Most of endpoints we test use default return type {@link ResponseEntity}
     *
     * @param requestBuilder GET/POST/whatever {@link RequestBuilder}, same as for {@link MockMvc#perform(RequestBuilder)}
     * @return {@link ResultActions} on which you later can perform verification operations (like expected status code,
     * content and so on)
     * @throws Exception same as from {@link MockMvc#perform(RequestBuilder)}
     */
    public ResultActions performAsync(RequestBuilder requestBuilder) throws Exception {
        return performAsync(requestBuilder, ResponseEntity.class);
    }

    /**
     * Similar to {@link MockMvc#perform(RequestBuilder)}, but for endpoints which are expected to be executed
     * asynchronously. This implementation starts and waits for async execution and verifies the expected enpoint
     * result class ({@code expectedResultClass}).
     *
     * @param requestBuilder      GET/POST/whatever {@link RequestBuilder}, same as for {@link MockMvc#perform(RequestBuilder)}
     * @param expectedResultClass expected class type for the endpoint return result.
     * @return {@link ResultActions} on which you later can perform verification operations (like expected status code,
     * content and so on)
     * @throws Exception same as from {@link MockMvc#perform(RequestBuilder)}
     */
    public ResultActions performAsync(RequestBuilder requestBuilder, Class<?> expectedResultClass) throws Exception {
        MvcResult asyncMvcResult = mockMvc.perform(requestBuilder)
                .andExpect(request().asyncStarted())
                .andExpect(request().asyncResult(instanceOf(expectedResultClass)))
                .andReturn();

        return mockMvc.perform(asyncDispatch(asyncMvcResult));
    }
}
