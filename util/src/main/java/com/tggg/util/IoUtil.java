package com.tggg.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtil {
    private static final int BUFFER_SIZE = 1500;

    /**
     * Silently close the given {@link Closeable}s, ignoring any {@link IOException}.<br/> {@code null} objects are ignored.
     *
     * @param toClose The {@link Closeable}s to close.
     */
    public static void closeSilently(Closeable... toClose) {
        for (Closeable closeable : toClose) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
    }

    /**
     * Copy the contents of the given {@link InputStream} into the given {@link OutputStream}.<br/>
     * Note: the given {@link InputStream} and {@link OutputStream} won't be closed.
     *
     * @param in  The {@link InputStream} to read.
     * @param out The {@link OutputStream} to write to.
     * @return the actual number of bytes that were read.
     * @throws IOException If a error occurs while reading or writing.
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        long res = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            out.flush();
            res += read;
        }
        return res;
    }

    /**
     * Fully reads the given {@link InputStream} into a {@link String}.<br/>
     * The encoding inside the {@link InputStream} is assumed to be {@code UTF-8}.<br/>
     * Note: the given {@link InputStream} won't be closed.
     *
     * @param in The {@link InputStream} to read.
     * @return a String containing the contents of the given {@link InputStream}.
     * @throws IOException If a error occurs while reading.
     */
    public static String readFully(InputStream in) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, read, "utf-8"));
        }
        return stringBuilder.toString();
    }

    public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // get rotate degree
        int rotate = CameraUtil.readPictureDegree(filePath);

        if (rotate != 0) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            return CameraUtil.getRotateBitmap(bitmap, rotate);
        } else {
            return BitmapFactory.decodeFile(filePath, options);
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static void saveBitmap(Bitmap bm, File saveFile) {
        if (saveFile.exists()) {
            saveFile.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(saveFile);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
