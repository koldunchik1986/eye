package com.koldunchik1986.eye.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public final class HashUtils {
    private HashUtils() {}

    public static String calculateMD5(InputStream is) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance(AppConstants.HASH_ALGORITHM_MD5);
            byte[] buffer = new byte[AppConstants.IO_BUFFER_SIZE];
            int read;
            while ((read = is.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String calculateFileHash(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return calculateMD5(fis);
        }
    }
}