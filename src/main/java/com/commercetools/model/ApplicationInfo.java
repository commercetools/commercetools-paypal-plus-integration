package com.commercetools.model;

import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

/**
 * Display build info from the manifest.
 *
 * <b>Note:</b> {@code Implementation-Title} and {@code Implementation-Version} must be set during the build, usually
 * in build script (see {@code /build.gradle}). Otherwise the value will be displayed as "undefined" (for example, in
 * the tests, IDE debug mode, or when run gradle task, like {@code bootRun}).
 */
@Component
public class ApplicationInfo {

    public static final String APP_INFO_KEY = "applicationInfo";

    private final String version;
    private final String title;

    public ApplicationInfo() {
        this.version = ofNullable(this.getClass().getPackage().getImplementationVersion()).orElse("undefined");
        this.title = ofNullable(this.getClass().getPackage().getImplementationTitle()).orElse("undefined");
    }

    /**
     * @return {@code Implementation-Version} from the manifest or "undefined" if empty.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return {@code Implementation-Title} from the manifest or "undefined" if empty.
     */
    public String getTitle() {
        return title;
    }
}
