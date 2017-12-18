package com.commercetools.http.converter.json;

import com.commercetools.config.ApplicationConfiguration;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static com.commercetools.testUtil.JsonAssertUtil.assertJsonPath;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApplicationConfiguration.class)
public class PrettyGsonMessageConverterTest {

    /**
     * We ensure that default "gsonMessageConverter
     */
    @Autowired
    private PrettyGsonMessageConverter gsonMessageConverter;

    @Test
    public void writeInternal_withHashMapBody_returnsMinifiedJson() throws Exception {
        MockHttpOutputMessage mockOutput = new MockHttpOutputMessage();
        gsonMessageConverter.writeInternal(mockResponseMap(), null, mockOutput);

        assertThat(mockOutput.getBodyAsString()).hasLineCount(1);
        assertWrittenMessage(mockOutput);
    }

    @Test
    public void writeInternal_withPrettyFormattedBodyAndPrettyFalse_returnsMinifiedJson() throws Exception {
        MockHttpOutputMessage mockOutput = new MockHttpOutputMessage();
        gsonMessageConverter.writeInternal(PrettyFormattedBody.of(mockResponseMap(), false), null, mockOutput);
        assertThat(mockOutput.getBodyAsString()).hasLineCount(1);
        assertWrittenMessage(mockOutput);
    }

    @Test
    public void writeInternal_withPrettyFormattedBodyAndPrettyTrue_returnsPrettyFormattedJson() throws Exception {
        MockHttpOutputMessage mockOutput = new MockHttpOutputMessage();
        gsonMessageConverter.writeInternal(PrettyFormattedBody.of(mockResponseMap(), true), null, mockOutput);
        assertThat(mockOutput.getBodyAsString().split("[\n\r]+").length).isGreaterThanOrEqualTo(4);
        assertWrittenMessage(mockOutput);
    }

    private Map<?, ?> mockResponseMap() {
        return ImmutableMap.of("aaa", "bbb", "ccc", asList(1, 2, 3));
    }

    /**
     * Asserts {@code mockOutput} contains the values from {@link #mockResponseMap()}
     *
     * @param mockOutput written {@link MockHttpOutputMessage} where to verify the content
     */
    private void assertWrittenMessage(MockHttpOutputMessage mockOutput) {
        String bodyAsString = mockOutput.getBodyAsString();
        assertJsonPath(bodyAsString, "$.aaa", is("bbb"));
        assertJsonPath(bodyAsString, "$.ccc", contains(1, 2, 3));
        assertJsonPath("{\"arrays\":{\"arrayKey\": [1, 2, 3]}}", "$.arrays.arrayKey", contains(1, 2, 3));
    }

    @Test
    public void supports_differentObjects() throws Exception {
        assertThat(gsonMessageConverter.supports(PrettyFormattedBody.class)).isTrue();

        // verify our overriding has not changed default behavior
        assertThat(gsonMessageConverter.supports(HashMap.class)).isTrue();
    }

    @Test
    public void canWrite_differentObjects() throws Exception {
        assertThat(gsonMessageConverter.canWrite(PrettyFormattedBody.class, MediaType.APPLICATION_JSON)).isTrue();
        assertThat(gsonMessageConverter.canWrite(PrettyFormattedBody.class, MediaType.APPLICATION_JSON_UTF8)).isTrue();

        // verify our overriding has not changed default behavior
        assertThat(gsonMessageConverter.canWrite(HashMap.class, MediaType.APPLICATION_JSON)).isTrue();
        assertThat(gsonMessageConverter.canWrite(HashMap.class, MediaType.APPLICATION_JSON_UTF8)).isTrue();

        assertThat(gsonMessageConverter.canWrite(MapType.class, HashMap.class, MediaType.APPLICATION_JSON)).isTrue();
        assertThat(gsonMessageConverter.canWrite(MapType.class, HashMap.class, MediaType.APPLICATION_JSON_UTF8)).isTrue();
    }

}