package com.koldunchik1986.eye.util;

public final class AppConstants {
    private AppConstants() {}

    public static final int REQUEST_CODE_PERMISSION = 100;
    public static final int RESULTS_PER_PAGE = 50;
    public static final long SEARCH_TYPE_HINT_DELAY_MS = 3000L;

    // IO
    public static final int IO_BUFFER_SIZE = 8192;
    public static final String HASH_ALGORITHM_MD5 = "MD5";

    // Files
    public static final String CSV_DIR_NAME = "csv";
    public static final String EXT_CSV = ".csv";
    public static final String EXT_IDX = ".idx";
    public static final String DEFAULT_IMPORTED_PREFIX = "imported_";

    // File picker
    public static final String[] PICKER_MIME_TYPES = new String[] {
            "text/plain", "text/csv", "application/csv", "application/vnd.ms-excel", "*/*"
    };

    // UI
    public static final int DIALOG_PADDING_LR_PX = 32;
    public static final int DIALOG_PADDING_TB_PX = 16;

    // Parsing
    public static final int ID_PREFIX_LENGTH = 2; // length of "id"
    public static final long MAPPING_DIALOG_POLL_INTERVAL_MS = 100L;

    // CSV normalization
    public static final char TARGET_CSV_DELIMITER = ';';
    public static final char[] CANDIDATE_DELIMITERS = new char[] {';', '|', ',', '\t'};
}