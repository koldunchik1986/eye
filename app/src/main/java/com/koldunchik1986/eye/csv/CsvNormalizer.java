package com.koldunchik1986.eye.csv;

import com.koldunchik1986.eye.util.AppConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CsvNormalizer {
    private CsvNormalizer() {}

    public static char detectDelimiter(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            int limit = 20;
            while (limit-- > 0 && (line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
            if (lines.isEmpty()) return AppConstants.TARGET_CSV_DELIMITER;

            char best = AppConstants.TARGET_CSV_DELIMITER;
            int bestScore = -1;
            for (char cand : AppConstants.CANDIDATE_DELIMITERS) {
                int totalCols = 0;
                int validLines = 0;
                for (String l : lines) {
                    int cols = naiveSplitCount(l, cand);
                    if (cols > 1) {
                        totalCols += cols;
                        validLines++;
                    }
                }
                int score = (validLines == 0) ? 0 : totalCols;
                if (score > bestScore) {
                    bestScore = score;
                    best = cand;
                }
            }
            return best;
        }
    }

    private static int naiveSplitCount(String line, char delimiter) {
        int count = 1;
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                count++;
            }
        }
        return count;
    }

    public static boolean normalize(InputStream inputStream, OutputStream outputStream, char sourceDelimiter, char targetDelimiter) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            StringBuilder out = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                List<String> fields = parseCsvLine(line, sourceDelimiter);
                for (int i = 0; i < fields.size(); i++) {
                    String field = fields.get(i);
                    String escaped = escapeField(field, targetDelimiter);
                    out.append(escaped);
                    if (i < fields.size() - 1) out.append(targetDelimiter);
                }
                out.append('\n');
                if (out.length() >= 64 * 1024) {
                    outputStream.write(out.toString().getBytes(StandardCharsets.UTF_8));
                    out.setLength(0);
                }
            }
            if (out.length() > 0) {
                outputStream.write(out.toString().getBytes(StandardCharsets.UTF_8));
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static List<String> parseCsvLine(String line, char delimiter) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // escaped quote
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == delimiter && !inQuotes) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields;
    }

    private static String escapeField(String field, char targetDelimiter) {
        if (field == null) field = "";
        String f = field.trim();
        boolean mustQuote = f.indexOf(targetDelimiter) >= 0 || f.indexOf('"') >= 0 || f.indexOf('\n') >= 0 || f.indexOf('\r') >= 0;
        if (!mustQuote) return f;
        String escaped = f.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}