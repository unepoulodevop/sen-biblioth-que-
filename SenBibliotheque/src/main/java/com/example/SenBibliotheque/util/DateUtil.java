package com.example.SenBibliotheque.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "N/A";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : "N/A";
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : "N/A";
    }

    public static LocalDateTime parseDate(String dateString) {
        try {
            return LocalDateTime.parse(dateString + " 00:00:00",
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}