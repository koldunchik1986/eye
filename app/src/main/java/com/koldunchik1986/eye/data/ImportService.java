package com.koldunchik1986.eye.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.koldunchik1986.eye.domain.CsvIndexBuilder;
import com.koldunchik1986.eye.util.HashUtils;

import java.io.*;
import java.util.Locale;

public class ImportService {
    private final Context context;
    private final File csvDir;
    private final CsvIndexBuilder indexBuilder;

    public ImportService(Context context, File csvDir, CsvIndexBuilder indexBuilder) {
        this.context = context.getApplicationContext();
        this.csvDir = csvDir;
        this.indexBuilder = indexBuilder;
    }

    public String getFileName(Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String result = null;
        try (Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex);
                }
            }
        } catch (Exception ignored) {}
        if (result == null) {
            result = new File(uri.getPath()).getName();
        }
        return result;
    }

    public File getUniqueFileForSaveByHash(Uri uri, String originalFileName) throws IOException {
        String name = originalFileName.toLowerCase(Locale.ROOT).endsWith(".csv")
                ? originalFileName
                : originalFileName + ".csv";

        File targetFile = new File(csvDir, name);
        String baseName = name.substring(0, name.length() - 4);
        int counter = 1;

        String newFileHash = calculateFileHash(uri);
        if (newFileHash == null) return targetFile;

        while (targetFile.exists()) {
            String existingHash = HashUtils.calculateFileHash(targetFile);
            if (newFileHash.equals(existingHash)) {
                return null;
            }
            targetFile = new File(csvDir, baseName + "_" + counter + ".csv");
            counter++;
        }
        return targetFile;
    }

    public boolean saveFileFromUri(Uri uri, File outputFile) {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(outputFile)) {
            if (is == null) return false;
            byte[] buffer = new byte[com.koldunchik1986.eye.util.AppConstants.IO_BUFFER_SIZE];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void createIndex(File outputFile, String[] headers) {
        indexBuilder.createSearchIndex(outputFile, headers);
    }

    private String calculateFileHash(Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            if (is == null) return null;
            return HashUtils.calculateMD5(is);
        } catch (IOException e) {
            return null;
        }
    }
}


