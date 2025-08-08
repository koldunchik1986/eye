package com.koldunchik1986.eye.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvFileRepository {
    private final File csvDir;

    public CsvFileRepository(File csvDir) {
        this.csvDir = csvDir;
    }

    public List<File> listCsvFiles() {
        File[] files = csvDir.listFiles((dir, name) -> name.endsWith(".csv"));
        if (files == null || files.length == 0) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(files));
    }

    public File getIndexFile(File csvFile) {
        return new File(csvFile.getParent(), csvFile.getName() + com.koldunchik1986.eye.util.AppConstants.EXT_IDX);
    }

    public boolean delete(File csvFile) {
        boolean csvDeleted = csvFile.delete();
        File indexFile = getIndexFile(csvFile);
        if (indexFile.exists()) indexFile.delete();
        return csvDeleted;
    }

    public boolean rename(File csvFile, File targetFile) {
        boolean csvRenamed = csvFile.renameTo(targetFile);
        if (!csvRenamed) return false;
        File oldIndex = getIndexFile(csvFile);
        File newIndex = new File(targetFile.getParent(), targetFile.getName() + ".idx");
        if (oldIndex.exists()) {
            // ignore result, best-effort
            //noinspection ResultOfMethodCallIgnored
            oldIndex.renameTo(newIndex);
        }
        return true;
    }

    public static Uri getShareUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}