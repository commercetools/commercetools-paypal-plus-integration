package com.commercetools.pspadapter.tenant;

import javax.annotation.Nonnull;

import static java.lang.String.format;

public final class TenantLoggerUtil {

    /**
     * Create a logger with name which consists of full qualified {@code clazz} name
     * and the suffix "[tenant &lt;name&gt;]".
     *
     * @param clazz      Class where the logger is used
     * @param tenantName tenant name to attach to the class name.
     * @return Concatenated FQDN and the {@code tenantName} wrapped to "[tenant &lt;name&gt;]" construction.
     */
    public static String createLoggerName(@Nonnull Class clazz, @Nonnull String tenantName) {
        return format("%s[tenant %s]", clazz.getName(), tenantName);
    }

    private TenantLoggerUtil() {
    }
}
