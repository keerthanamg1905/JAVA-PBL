package com.equishare.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility for parsing and formatting dates.
 */
public class DateUtils {
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static String todayString() {
        return LocalDate.now().format(ISO_FORMATTER);
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), ISO_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String formatDisplay(String isoDateStr) {
        if (isoDateStr == null || isoDateStr.trim().isEmpty()) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(isoDateStr.trim(), ISO_FORMATTER);
            return date.format(DISPLAY_FORMATTER);
        } catch (Exception e) {
            return isoDateStr;
        }
    }
}
