package com.commercetools.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Manipulation/conversion between time-related entities
 */
public class TimeUtil {

    public static ZonedDateTime toZonedDateTime(String rfc3339Time) {
        Instant updateTime = Instant.parse(rfc3339Time);
        return updateTime.atZone(ZoneOffset.UTC);
    }

    private TimeUtil() {
    }
}