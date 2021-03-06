package com.gangle.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * Creates an empty temporary file using the given base name and suffix as part of the file name.<br/>
     * If {@code suffix} is {@code null}, {@code ".tmp"} is used.
     *
     * @param baseName The base name to use (must not be {@code null}).
     * @param suffix   The suffix to use (can be {@code null}).
     * @return An empty temporary file.
     * @throws RuntimeException         If the file could not be created.
     * @throws IllegalArgumentException If {@code baseName} is {@code null}.
     */
    public static File newTemporaryFile(Context context, String baseName, String suffix) {
        if (baseName == null) throw new IllegalArgumentException("baseName must not be null");
        File cacheDir = EnvironmentUtil.getExternalCacheDir(context);
        File res = new File(cacheDir, baseName + (suffix == null ? ".tmp" : suffix));
        if (res.exists()) res.delete();
        try {
            res.createNewFile();
            // This may very well be useless
            res.deleteOnExit();
            Log.d(TAG, "newTemporaryFile res=" + res);
            return res;
        } catch (IOException e) {
            Log.w(TAG, "Could not create a temporary file at " + res, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an empty temporary file using a unique id as the base name and the given suffix as part of the file name.<br/>
     * If {@code suffix} is {@code null}, {@code ".tmp"} is used.
     *
     * @param suffix The suffix to use (can be {@code null}).
     * @return An empty temporary file.
     * @throws RuntimeException If the file could not be created.
     */
    public static File newTemporaryFile(Context context, String suffix) {
        return newTemporaryFile(context, UUID.randomUUID().toString(), suffix);
    }

    /**
     * Get a string suitable to be used as a file name.<br/>
     * This will replace characters that cannot be used in a file name (for instance '/' or '='), with the given replacement character, or with nothing if
     * {@code null} is given.
     *
     * @param originalName    The original name.
     * @param replacementChar The replacement character to use or {@code null} to just strip the bad characters.
     * @return A new string equal to {@code originalName} with the bad characters stripped or replaced.
     */
    public static String getValidFileName(String originalName, Character replacementChar) {
        int len = originalName.length();
        StringBuilder res = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = originalName.charAt(i);
            if (c == ' ' || c == '-' || c == '_' || c == '.' || c == ',' || c == '(' || c == ')') {
                res.append(c);
            } else if (c == '\u00E9' || c == '\u00E0' || c == '\u00E9' || c == '\u00E7' || c == '\u00F4' || c == '\u00EE') {
                res.append(c);
            } else if ('0' <= c && c <= '9') {
                res.append(c);
            } else if ('a' <= c && c <= 'z') {
                res.append(c);
            } else if ('A' <= c && c <= 'Z') {
                res.append(c);
            } else if (replacementChar != null) {
                res.append(replacementChar.charValue());
            }
        }
        return res.toString();
    }

    /**
     * Equivalent of calling {@code getValidFilename(originalName, null)}.
     */
    public static String getValidFileName(String originalName) {
        return getValidFileName(originalName, null);
    }

    /**
     * A criteria to delete files if they are older than a given max age.
     */
    public class ExpiredFileFilter implements FileFilter {
        private long mMaxAgeMs;

        public ExpiredFileFilter(long maxAgeMs) {
            mMaxAgeMs = maxAgeMs;
        }

        @Override
        public boolean accept(File file) {
            return file.lastModified() < System.currentTimeMillis() - mMaxAgeMs;
        }
    }

    /**
     * Recursively delete a file or directory.<br/>
     * An optional {@link FileFilter} can be given to choose to delete only certain files ({@link FileFilter#accept(File)} returning {@code true} means the file
     * should be deleted).<br/>
     * If a filter is given, it will only be used on files, not directories and because of that, directories will not be deleted. If {@code null} is given, then
     * files <strong>and</strong> directories are deleted.
     *
     * @param fileOrDirectory The file or directory to delete.
     * @param criteria        The criteria to use to choose to delete only certain files, or {@code null} to delete all of them.
     */
    public static void deleteRecursively(File fileOrDirectory, FileFilter criteria) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursively(child, criteria);
            }
            if (criteria == null) {
                boolean ok = fileOrDirectory.delete();
                Log.d(TAG, "deleted directory " + fileOrDirectory + (ok ? " ok" : " NOT ok"));
            }
        } else {
            if (criteria == null || criteria.accept(fileOrDirectory)) {
                boolean ok = fileOrDirectory.delete();
                Log.d(TAG, "deleted file " + fileOrDirectory + (ok ? " ok" : " NOT ok"));
            }
        }
    }

    /**
     * Recursively delete a file or directory.
     *
     * @param fileOrDirectory The file or directory to delete.
     */
    public static void deleteRecursively(File fileOrDirectory) {
        deleteRecursively(fileOrDirectory, null);
    }

    /**
     * Copy a file.
     *
     * @param from The path of the source file to copy.
     * @param to   The destination path (must include the file name).
     * @throws IOException If a error occurs while reading or writing.
     */
    public static void copy(String from, String to) throws IOException {
        copy(new File(from), new File(to));
    }

    /**
     * Copy a file.
     *
     * @param from The source file to copy.
     * @param to   The destination file (must be a file, not a directory).
     * @throws IOException If a error occurs while reading or writing.
     */
    public static void copy(File from, File to) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(from));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(to));
        try {
            IoUtil.copy(in, out);
            out.flush();
        } finally {
            IoUtil.closeSilently(in, out);
        }
    }

    /**
     * 保存Bitmap到sdcard
     *
     * @param b
     */
    public static void saveBitmap(String path, Bitmap b) {

        long dataTake = System.currentTimeMillis();
        String jpegName = path + "/" + dataTake + ".jpg";
        Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap成功");
        } catch (IOException e) {
            Log.i(TAG, "saveBitmap:失败");
            e.printStackTrace();
        }

    }
}
