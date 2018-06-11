package com.gangle.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtil {

    private static final long[] VIBRATE_PATTERN = {100, 500}; // show heads up notification for Lollipop and above

    /**
     * byte数组转成integer
     */
    public static int byte2int(byte[] byt) {
        return (Integer) (byt[0] & 0xff | (byt[1] & 0xff) << 8 | (byt[2] & 0xff) << 16 | (byt[3] & 0xff) << 24);
    }

    /**
     * integer转成byte数组
     */
    public static byte[] int2byte(int number) {
        byte[] byt = new byte[4];
        byt[0] = (byte) (number & 0xff);
        byt[1] = (byte) (number >> 8 & 0xff);
        byt[2] = (byte) (number >> 16 & 0xff);
        byt[3] = (byte) (number >> 24 & 0xff);
        return byt;
    }

    /**
     * 判断GPS是否打开
     */
    public static boolean isGPSEnable(Context context) {
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    /**
     * 打开GPS
     *
     * @param context
     */
    public static void openGPS(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // The Android SDK doc says that the location settings activity
            // may not be found. In that case show the general settings.
            // General settings activity
            intent.setAction(Settings.ACTION_SETTINGS);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 获取屏幕宽度和高度，单位为px
     *
     * @param context
     * @return
     */
    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        Log.i("", "Screen---Width = " + w_screen + " Height = " + h_screen + " densityDpi = " + dm.densityDpi);
        return new Point(w_screen, h_screen);

    }


    /**
     * 获取屏幕长宽比
     *
     * @param context
     * @return
     */
    public static float getScreenRate(Context context) {
        Point P = getScreenMetrics(context);
        float H = P.y;
        float W = P.x;
        return (H / W);
    }

    /**
     * dip转pixel
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * pixel转dip
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转pixel
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 震动
     */
    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_PATTERN, -1);
    }

    /**
     * 返回当前是否有音乐在播放
     *
     * @param context
     * @return
     */
    public static boolean isMusicActive(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isMusicActive();
    }

    /**
     * 播放固定的铃声
     *
     * @param context
     * @param ring_id
     */
//    public static void playParkingRing(Context context, int ring_id) {
//        SoundPool soundPool;
//        if (false && (android.os.Build.VERSION.SDK_INT) >= 21) {
//            SoundPool.Builder builder = new SoundPool.Builder();
//            builder.setMaxStreams(2);
//            builder.setAudioAttributes(new AudioAttributes.Builder()
//                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .build());
//            soundPool = builder.build();
//        } else {
//            soundPool = new SoundPool(2, AudioManager.STREAM_NOTIFICATION, 0);
//        }
//
//        final int sourceid = soundPool.load(context, ring_id, 1);
//        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                soundPool.play(sourceid, 1f, 1f, 0, 0, 1);
//            }
//        });
//
//    }

    /**
     * 自启动App
     *
     * @param context
     * @param activityCls， 启动app的Activity
     */
    public static void restartApp(Context context, Class activityCls) {
        restartApp(context, activityCls, null);
    }

    /**
     * 自启动App
     *
     * @param context
     * @param activityCls 启动app的Activity
     * @param extraData   附带的数据
     */
    public static void restartApp(Context context, Class activityCls, Bundle extraData) {
        Intent iStartActivity = new Intent(context, activityCls);
        if (extraData != null) {
            iStartActivity.putExtras(extraData);
        }
        int pendingIntentId = 283234;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, iStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);
        System.exit(0);
    }

    public static String getMetaData(Context context, String key) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Object object = ai.metaData.get(key);
            Log.v("", "meta data key:" + key + ",  value:" + object.toString());
            if (object != null) {
                return object.toString();
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 判断屏幕是否“亮”
     *
     * @param context
     * @return
     */
    public static boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screen = pm.isScreenOn();
        return pm.isScreenOn();
    }

    /**
     * 判断邮箱格式有效性
     *
     * @param email
     * @return
     */
    public static boolean checkEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("\\A[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\z");
            return pattern.matcher(email).matches();
        }
    }


    /**
     * 计算2个圆的公切角度
     *
     * @param distance 2圆的圆心距离
     * @param radiusA  圆A的半径
     * @param radiusB  圆B的半径
     * @return 公切角度，取值范围0~360.单位度数。
     * @throws Exception
     */
    public static double commonTangentDegree(double distance, double radiusA, double radiusB) throws Exception {
        if (distance < 0 || radiusA <= 0 || radiusB <= 0) {
            throw new NegativeException();
        }

        if (radiusA + radiusB > distance) {
            if (distance == 0f || distance <= Math.abs(radiusA - radiusB)) {
                return 359.99f;
            } else {
                double shareArea = sharedArea(distance, radiusA, radiusB);
                double maxArea = Math.PI * Math.max(radiusA, radiusB) * Math.max(radiusA, radiusB);
                return Math.min(180 + 180 * shareArea / maxArea, 359.99f);
            }
        }

        double lineBC = (distance * radiusB) / (radiusB + radiusA);
        double angelC = Math.asin(radiusB / lineBC) * 180 / Math.PI;
        Log.d("", "Angel C:" + angelC + ", lineBC:" + lineBC);
        return angelC * 2;
    }

    /**
     * 计算2圆相交时的面积
     *
     * @param distance
     * @param r1
     * @param r2
     * @return
     */
    public static double sharedArea(double distance, double r1, double r2) {
        double alpha, area;
        if (distance > r1 + r2) {
            return 0;
        }

        alpha = Math.acos((distance * distance + r1 * r1 - r2 * r2) / (2 * distance * r1));//余弦定理取得相交弧所对本圆的圆心角
        area = alpha * r1 * r1;//本圆扇形面积
        alpha = Math.acos((distance * distance + r2 * r2 - r1 * r1) / (2 * distance * r2));//余弦定理取得相交弧所对另一圆的圆心角
        area += alpha * r2 * r2;//另一圆的扇形面积
        double s = (distance + r1 + r2) / 2;//海伦公式之s
        area -= Math.sqrt(s * (s - distance) * (s - r1) * (s - r2)) * 2;//两扇形面积减去两三角形面积即为交集
        return area;
    }

    /**
     * 获取
     *
     * @param val
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5(String val) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(val.getBytes());
        byte[] m = md5.digest();//加密
        StringBuilder hex = new StringBuilder(m.length * 2);
        for (byte b : m) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF).toUpperCase());
        }
        return hex.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] r = mac.doFinal(data.getBytes());
        return Base64.encodeToString(r, Base64.DEFAULT).trim();
    }

    /**
     * 设置group内的view的enable状态
     * @param group
     * @param enable
     */
    public static void setGroupLayoutEnable(ViewGroup group, boolean enable) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View view = group.getChildAt(i);
            view.setEnabled(enable); // Or whatever you want to do with the view.
        }
    }


    public static class InterSectionException extends Exception {
    }

    public static class NegativeException extends Exception {
    }
}

