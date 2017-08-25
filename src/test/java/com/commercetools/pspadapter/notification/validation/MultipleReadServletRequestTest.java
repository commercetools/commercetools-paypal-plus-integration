package com.commercetools.pspadapter.notification.validation;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.ServletInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleReadServletRequestTest {

    @Test
    public void shouldReturnNewStreamForEveryMethodCall() throws IOException {
        // set up
        String content = "test content";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(content.getBytes());
        MultipleReadServletRequest servlet = new MultipleReadServletRequest(mockRequest);

        // test
        ServletInputStream inputStream = servlet.getInputStream();
        String stringFromStream = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining(""));
        inputStream.close();

        ServletInputStream inputStream2 = mockRequest.getInputStream();
        String stringFromStream2 = new BufferedReader(new InputStreamReader(inputStream2))
                .lines()
                .collect(Collectors.joining(""));
        inputStream2.close();

        assertThat(content).isEqualTo(stringFromStream).isEqualTo(stringFromStream2);
    }
}