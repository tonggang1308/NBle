package com.tggg.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PreferenceUtil {

    public static boolean write(SharedPreferences sharedPreferences, String key, String value) {
        return sharedPreferences.edit().putString(key, value).commit();
    }

    public static boolean write(SharedPreferences sharedPreferences, String key, int value) {
        return sharedPreferences.edit().putInt(key, value).commit();
    }

    public static boolean write(SharedPreferences sharedPreferences, String key, long value) {
        return sharedPreferences.edit().putLong(key, value).commit();
    }

    public static boolean write(SharedPreferences sharedPreferences, String key, Boolean value) {
        return sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static String readString(SharedPreferences sharedPreferences, String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public static String readString(SharedPreferences sharedPreferences, String key) {
        return readString(sharedPreferences, key, null);
    }

    public static int readInt(SharedPreferences sharedPreferences, String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public static int readInt(SharedPreferences sharedPreferences, String key) {
        return readInt(sharedPreferences, key, 0);
    }

    public static long readLong(SharedPreferences sharedPreferences, String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static long readLong(SharedPreferences sharedPreferences, String key) {
        return readLong(sharedPreferences, key, 0);
    }

    public static boolean readBoolean(SharedPreferences sharedPreferences, String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static boolean readBoolean(SharedPreferences sharedPreferences, String key) {
        return readBoolean(sharedPreferences, key, false);
    }

    public static boolean remove(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.edit().remove(key).commit();
    }

    /**
     * 写入对象
     */
    public static boolean writeObject(SharedPreferences sharedPreferences, String key, Object value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.close();
            bos.flush();
            bos.close();
            String base64Str = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
            return sharedPreferences.edit().putString(key, base64Str).commit();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 得到对象
     */
    public static Object readObject(SharedPreferences sharedPreferences, String key) {
        Object obj = null;
        String value = sharedPreferences.getString(key, "");
        // 反序列化
        try {
            byte[] buffer = Base64.decode(value, Base64.DEFAULT);
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return obj;
    }

    /**
     * 获取默认的SharedPreference
     */
    public static SharedPreferences getDefaultSharedPreference(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences;
    }

    /**
     * 获取指定的SharedPreference
     */
    public static SharedPreferences getSharedPreferenceByFile(Context context, String fileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences;
    }


}
