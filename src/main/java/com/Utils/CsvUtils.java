package com.utils;

public class CsvUtils {

    private CsvUtils() {} // Private constructor to prevent instantiation

    public static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}
