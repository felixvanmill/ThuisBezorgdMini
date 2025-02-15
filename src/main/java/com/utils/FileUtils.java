package com.utils;

import org.springframework.web.multipart.MultipartFile;

public class FileUtils {

    /**
     * Validates if the uploaded file is a valid CSV.
     *
     * @param file The file to validate.
     */
    public static void validateCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty.");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file format. Please upload a .csv file.");
        }
        if (file.getSize() > 2 * 1024 * 1024) { // 2MB limit
            throw new IllegalArgumentException("File size exceeds the 2MB limit.");
        }
    }
}
