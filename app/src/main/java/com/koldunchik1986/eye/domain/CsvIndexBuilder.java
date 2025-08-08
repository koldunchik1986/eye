package com.koldunchik1986.eye.domain;

import com.koldunchik1986.eye.csv.CsvReader;

import java.io.*;

public class CsvIndexBuilder {
    public void createSearchIndex(File csvFile, String[] headers) {
        File indexFile = new File(csvFile.getParent(), csvFile.getName() + com.koldunchik1986.eye.util.AppConstants.EXT_IDX);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile)));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexFile)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) return;

            int telIndex = -1, nameIndex = -1, emailIndex = -1, tgIdIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].toLowerCase();
                if (h.contains("tel") || h.contains("phone")) telIndex = i;
                else if (h.contains("name") || h.contains("фио")) nameIndex = i;
                else if (h.contains("mail") || h.contains("email")) emailIndex = i;
                else if (h.contains("tg") || h.contains("telegram")) tgIdIndex = i;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("[;|]", -1);
                if (parts.length < headers.length) continue;

                StringBuilder searchableLine = new StringBuilder();

                if (telIndex != -1 && telIndex < parts.length) {
                    searchableLine.append(CsvReader.cleanField(parts[telIndex]).toLowerCase()).append(" ");
                }
                if (nameIndex != -1 && nameIndex < parts.length) {
                    searchableLine.append(CsvReader.cleanField(parts[nameIndex]).toLowerCase()).append(" ");
                }
                if (emailIndex != -1 && emailIndex < parts.length) {
                    searchableLine.append(CsvReader.cleanField(parts[emailIndex]).toLowerCase()).append(" ");
                }
                if (tgIdIndex != -1 && tgIdIndex < parts.length) {
                    searchableLine.append(CsvReader.cleanField(parts[tgIdIndex]).toLowerCase()).append(" ");
                }

                String indexLine = searchableLine.toString().trim();
                writer.write(indexLine.isEmpty() ? "" : indexLine);
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}