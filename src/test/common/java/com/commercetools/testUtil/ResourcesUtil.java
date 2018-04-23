package com.commercetools.testUtil;

public final class ResourcesUtil {
    public static String MOCK_ROOT_DIR = "mockData/";

    public static String resolveMockDataResource(String mockDataRelativePath) {
        return MOCK_ROOT_DIR + mockDataRelativePath;
    }

    private ResourcesUtil() {
    }
}