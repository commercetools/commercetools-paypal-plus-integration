package com.commercetools.util;

import javax.annotation.Nonnull;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Util for working with input/output operations
 */
public class IOUtil {

    private IOUtil() {
    }

    public static String getBody(@Nonnull HttpServletRequest request) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                return bufferedReader.lines().collect(Collectors.joining());
            }
        } else {
            return "";
        }
    }
}