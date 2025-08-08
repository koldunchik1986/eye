package com.koldunchik1986.eye.domain;

import android.os.Handler;
import android.os.Looper;

import com.koldunchik1986.eye.csv.CsvReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchEngine {
    public interface Callback {
        void onProgress(String fileName);
        void onPage(List<String> page, boolean hasMore);
        void onComplete();
        void onEmpty(String query);
        void onError(Throwable t);
    }

    private final File csvDir;
    private final int resultsPerPage;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SearchEngine(File csvDir, int resultsPerPage) {
        this.csvDir = csvDir;
        this.resultsPerPage = resultsPerPage;
    }

    public CancellationToken search(String query, Callback callback) {
        AtomicBoolean cancelled = new AtomicBoolean(false);
        executor.execute(() -> {
            try {
                String qLower = query.trim().toLowerCase();
                if (qLower.isEmpty()) {
                    mainHandler.post(() -> callback.onEmpty(query));
                    return;
                }

                File[] files = csvDir.listFiles((dir, name) -> name.endsWith(".csv"));
                if (files == null || files.length == 0) {
                    mainHandler.post(() -> callback.onEmpty(query));
                    return;
                }

                List<String> page = new ArrayList<>(resultsPerPage);
                int totalCount = 0;

                for (File file : files) {
                    if (cancelled.get()) break;
                    File indexFile = new File(file.getParent(), file.getName() + ".idx");
                    if (!indexFile.exists()) continue;

                    File f = file;
                    mainHandler.post(() -> callback.onProgress(f.getName()));

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile)))) {
                        String line;
                        int lineNumberInIdx = 0;
                        while (!cancelled.get() && (line = reader.readLine()) != null) {
                            lineNumberInIdx++;
                            if (line.contains(qLower)) {
                                String original = CsvReader.readLineFormatted(file, lineNumberInIdx);
                                if (original != null) {
                                    page.add(original);
                                    totalCount++;
                                    if (page.size() == resultsPerPage) {
                                        List<String> emit = new ArrayList<>(page);
                                        page.clear();
                                        boolean hasMore = true; // more may exist
                                        mainHandler.post(() -> callback.onPage(emit, hasMore));
                                    }
                                }
                            }
                        }
                    }
                }

                if (cancelled.get()) return;

                if (!page.isEmpty()) {
                    List<String> emit = new ArrayList<>(page);
                    boolean hasMore = false;
                    mainHandler.post(() -> callback.onPage(emit, hasMore));
                } else if (totalCount == 0) {
                    mainHandler.post(() -> callback.onEmpty(query));
                }

                mainHandler.post(callback::onComplete);
            } catch (Throwable t) {
                mainHandler.post(() -> callback.onError(t));
            }
        });

        return () -> cancelled.set(true);
    }

    public interface CancellationToken {
        void cancel();
    }
}