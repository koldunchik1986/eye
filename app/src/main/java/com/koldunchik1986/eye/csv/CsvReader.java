package com.koldunchik1986.eye.csv;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class CsvReader {
    private CsvReader() {}

    public static String getHeaderLine(File csvFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readLineFormatted(File csvFile, int targetLine) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)))) {
            String line;
            int currentLine = 0;
            while ((line = reader.readLine()) != null) {
                if (currentLine == targetLine) {
                    String[] parts = line.split("[;|]", -1);
                    String headerLine = getHeaderLine(csvFile);
                    if (headerLine == null) return null;

                    String[] headers = headerLine.trim().split("[;|]", -1);
                    StringBuilder result = new StringBuilder();
                    result.append("База: ").append(csvFile.getName()).append("\n");
                    for (int i = 0; i < headers.length; i++) {
                        String value = i < parts.length ? cleanField(parts[i]) : "";
                        result.append(headers[i]).append(": ")
                                .append(value.isEmpty() ? "отсутствует" : value)
                                .append("\n");
                    }
                    result.append("\n");
                    return result.toString();
                }
                currentLine++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CsvReader", "Ошибка чтения CSV: " + csvFile.getName(), e);
        }
        return null;
    }

    public static String cleanField(String s) {
        return s == null ? "" : s.trim().replaceAll("^\"|\"$", "");
    }
}


