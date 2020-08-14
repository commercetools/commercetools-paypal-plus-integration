package com.commercetools.util;

import javax.annotation.Nonnull;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Util for working with input/output operations
 */
public final class IOUtil {

    public static final Charset DEFAULT_CHARSET = UTF_8;

    public static String getBody(@Nonnull HttpServletRequest request) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, DEFAULT_CHARSET))) {
                return bufferedReader.lines().collect(Collectors.joining());
            }
        } else {
            return "";
        }
    }

    private IOUtil() {
    }
}