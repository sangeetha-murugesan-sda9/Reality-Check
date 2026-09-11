package com.comeon.assignment.realitycheck.util;

import lombok.extern.slf4j.Slf4j;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formats instants stored as epoch seconds into the format requested by the frontend team:
 * Example: 6 July 26 14:35
 */
@Slf4j
public final class PlayerTimeFormatter {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("d MMMM yy HH:mm", Locale.ENGLISH);

    private PlayerTimeFormatter() {
    }

    public static String format(long epochSeconds, String timezone) {
        ZonedDateTime local = Instant.ofEpochSecond(epochSeconds).atZone(resolveZone(timezone));
        return FORMAT.format(local);
    }

    private static ZoneId resolveZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException | NullPointerException e) {
            log.warn("Unrecognised player timezone '{}', falling back to UTC", timezone);
            return ZoneId.of("UTC");
        }
    }
}