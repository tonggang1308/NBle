package com.tggg.util;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * Created by henry on 3/30/15.
 */
public class DeviceUtil {

    /**
     * 手机MEI
     */
    private static final String IMEI = "IMEI";
    /**
     * 手机MAC
     */
    private static final String MAC = "MAC";
    /**
     * 操作系统
     */
    private static final String MOBILE_OS = "MOBILE_OS";
    /**
     * 屏幕分辨率
     */
    private static final String SCREEN = "SCREEN";

    /**
     * 手机品牌型号 *
     */
    private static final String AGENT = "AGENT";

    /**
     * SIM卡的运行商类型 *
     */
    private static final String SIMCARD_TYPE = "SIMCARD_TYPE";

    /**
     * 联网类型
     */
    private static final String NETWORK_TYPE = "NETWORK_TYPE";

    private static final String SIM = "SIM";

    /**
     * 日志产生的时间 *
     */
    private static final String VISIT_DATE = "VISIT_DATE";

    /**
     * CPU型号
     */
    private static final String CPU_TYPE = "CPU_TYPE";

    /**
     * CPU厂商
     */
    private static final String CPU_FIRM = "CPU_FIRM";

    /**
     * 内核版本
     */
    private static final String KERNEL_VERSION = "KERNEL_VERSION";

    /**
     * 系统语言
     */
    private static final String SYS_LANGUAGE = "SYS_LANGUAGE";

    /**
     * 是否有SD卡：1：有，0：无
     */
    private static final String HAS_SDCARD = "HAS_SDCARD";

    /**
     * SD卡剩余空间大小（long）
     */
    private static final String LEFT_SPACE_SDCARD = "LEFT_SPACE_SDCARD";

    /**
     * 手机内存剩余空间大小
     */
    private static final String LEFT_SPACE_PHONE = "LEFT_SPACE_PHONE";

    /**
     * 是否允许求未知源：1：是，0：否
     */
    private static final String ALLOW_UNKNOWN_RES = "ALLOW_UNKNOWN_RES";

    /**
     * 静默安装权限: 1：有，0：无
     */
    private static final String PERMISSION_SLIENT = "PERMISSION_SLIENT";

    /**
     * ROOT权限：1：有，0：无
     */
    private static final String PERMISSION_ROOT = "PERMISSION_ROOT";
    /**
     * 手机RAM
     */
    private static final String RAM = "RAM";
    /**
     * 手机可用RAM
     */
    public static final String RAM_AVAILABLE = "AVAILABLE_RAM";

    public DeviceUtil() {

    }

    /**
     * 获取设备信息
     *
     * @param context {@link Context}
     * @return Map {@link java.util.Map}
     * @author Henry
     * @date 2015-3-30
     */
    public static Map<String, String> getDeviceInfoMap(Context context) {
        if (context == null) {
            return null;
        }
        Map<String, String> deviceInfoMap = new HashMap<>();
        String hasSDCard = hasSDCard() ? "YES" : "NO";
        long leftSpaceSDCard = getSDCardLeftSpace();
        long leftSpacePhone = getSystemLeftSpace();
        String allowUnknownRes = isAllowUnknownRes(context) ? "YES" : "NO";
        String permissionSilent = hasSilentInstallPermission(context) ? "YES" : "NO";
        String permissionRoot = isRooted() ? "YES" : "NO";
        deviceInfoMap.put(MAC, getMacAddress(context));
        deviceInfoMap.put(IMEI, getIMEI(context));
        deviceInfoMap.put(MOBILE_OS, getOSVersion());
        deviceInfoMap.put(SCREEN, getScreen(context));
        deviceInfoMap.put(SIMCARD_TYPE, getSimOperatorName(context));
        deviceInfoMap.put(SIM, getSimNumber(context));
        deviceInfoMap.put(AGENT, getBrand() + "|" + getModel());
        deviceInfoMap.put(NETWORK_TYPE, getNetworkTypeName(context));
//        deviceInfoMap.put(VISIT_DATE, LoggerUtils.dateToString("yyyy-MM-dd-HH-mm-SS"));
        deviceInfoMap.put(CPU_TYPE, getCpuInfo()[0]);
        deviceInfoMap.put(CPU_FIRM, getCpuInfo()[1]);
        deviceInfoMap.put(KERNEL_VERSION, "");
        deviceInfoMap.put(SYS_LANGUAGE, getSysLanguage());
        deviceInfoMap.put(HAS_SDCARD, hasSDCard);
        deviceInfoMap.put(LEFT_SPACE_SDCARD, String.valueOf(leftSpaceSDCard));
        deviceInfoMap.put(LEFT_SPACE_PHONE, String.valueOf(leftSpacePhone));
        deviceInfoMap.put(ALLOW_UNKNOWN_RES, allowUnknownRes);
        deviceInfoMap.put(PERMISSION_SLIENT, permissionSilent);
        deviceInfoMap.put(PERMISSION_ROOT, permissionRoot);
        deviceInfoMap.put(RAM, String.valueOf(getTotalMemory()));
        deviceInfoMap.put(RAM_AVAILABLE, String.valueOf(getAvailableMemory(context)));
        return deviceInfoMap;
    }


    /**
     * 获取IMSI 编号
     *
     * @param context {@link Context}
     * @return String 国际移动用户识别码是区别移动用户的标志，储存在SIM卡中
     * @author Henry
     * @date 2015-3-30
     */
    public static String getSimNumber(Context context) {
        if (context == null) {
            return null;
        }
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telManager.getSubscriberId();
    }


    /**
     * 获取运营商名称
     *
     * @param context {@link Context}
     * @return String 中国移动,中国联通,中国电信
     * @author Henry
     * @date 2015-3-30
     */
    public static String getSimOperatorName(Context context) {
        if (context == null) {
            return null;
        }
        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = telManager.getSubscriberId();

        if (!TextUtils.isEmpty(imsi)) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                return "中国移动";
            } else if (imsi.startsWith("46001")) {
                return "中国联通";
            } else if (imsi.startsWith("46003")) {
                return "中国电信";
            }
        }
        return "unknow";

    }

    /**
     * 获取设备mac地址
     *
     * @param context {@link Context}
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getMacAddress(Context context) {
        if (context == null) {
            return null;
        }
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return "";
        }
        WifiInfo info = wifi.getConnectionInfo();
        if (null != info) {
            return info.getMacAddress();
        }
        return "";
    }

    /**
     * 获取设备IMEI编号
     *
     * @param context {@link Context}
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getIMEI(Context context) {
        if (context == null) {
            return null;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = getSimNumber(context);
        }
        if (null != deviceId && deviceId.length() > 20) {
            deviceId = deviceId.substring(0, 20);
        }
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = "000000000000000";
        }
        return deviceId;
    }

    /**
     * 获取手机型号
     *
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机品牌
     *
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取android 版本
     *
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取国家代码
     *
     * @param context {@link Context}
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getCountryCode(Context context) {
        if (context == null) {
            return null;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSimCountryIso();
    }

    /**
     * 获取当前设备语言
     *
     * @param context {@link Context}
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getLanguage(Context context) {
        if (context == null) {
            return null;
        }
        return context.getResources().getConfiguration().locale.getLanguage();
    }

    /**
     * 获取移动网络运营商名称
     *
     * @param context {@link Context}
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getNetworkOperatorName(Context context) {
        if (context == null) {
            return null;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkOperatorName();
    }

    /**
     * 获取屏幕分辨率(像素)
     *
     * @param windowManager {@link WindowManager}
     * @return int
     * @author Henry
     * @date 2015-3-30
     */
    private static int getTruePixelsHeight(WindowManager windowManager) {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2) {
                // 13表示3.2系统，需要调用getRealHeight获取真实的屏幕高度
                Display display = windowManager.getDefaultDisplay();
                Class<?> c = Class.forName("android.view.Display");
                Method method = c.getMethod("getRealHeight");
                int height = (Integer) method.invoke(display);
                return height;
            }
        } catch (Exception e) {

        }
        return 0; // 返回0，表示此函数返回值不可用
    }

    /**
     * 获取屏幕分辨率
     *
     * @return int[] [0] width [1]height
     * @author Henry
     * @date 2015-3-30
     */
    public static int[] getDisplayResolution(Context context) {
        if (context == null) {
            return null;
        }
        int[] resolution = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int trueHeight = getTruePixelsHeight(windowManager);
        if (trueHeight > 0) {
            height = trueHeight;
        }

        resolution[0] = width;
        resolution[1] = height;
        return resolution;
    }

    /**
     * 获取屏幕分辨率
     *
     * @param context {@link Context}
     * @return String width*height
     * @author Henry
     * @date 2015-3-30
     */
    public static String getScreen(Context context) {
        if (context == null) {
            return null;
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int trueHeight = getTruePixelsHeight(windowManager);
        if (trueHeight > 0) {
            height = trueHeight;
        }

        return String.valueOf(width) + "*" + String.valueOf(height);
    }

    /**
     * 获取屏幕密度
     *
     * @param context {@link Context}
     * @return int[] int[0] desity 屏幕密度（像素比例：0.75/1.0/1.5/2.0） , int[1]
     * desityDIP 屏幕密度（每寸像素：120/160/240/320）
     * @author Henry
     * @date 2015-3-30
     */
    public static int[] getDesity(Context context) {
        if (context == null) {
            return null;
        }
        int[] desitys = new int[2];
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        desitys[0] = (int) dm.density;
        desitys[1] = dm.densityDpi;
        return desitys;
    }

    /**
     * 获取手机当前IP地址
     *
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("IpAddress", ex.toString());
        }
        return null;
    }

    /**
     * 获取SDK版本号
     *
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机号码
     *
     * @param context {@link Context}
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getPhoneNumber(Context context) {
        if (context == null) {
            return null;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    /**
     * 判断屏幕是否关闭
     *
     * @param context {@link Context}
     * @return boolean
     * @author Henry
     * @date 2015-3-30
     */
    public static boolean isScreenOff(Context context) {
        if (context == null) {
            return false;
        }
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    /**
     * 获取CPU型号和CPU厂商
     *
     * @return cpuInfo[0]:CPU型号,cpuInfo[1]:CPU厂商
     * @author Henry
     * @date 2015-3-30
     */
    public static String[] getCpuInfo() {
        String[] cpuInfo = null;
        try {
            cpuInfo = new String[2];
            FileReader fileReader = new FileReader("/proc/cpuinfo");
            BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
            // get cpu Processor
            String line = bufferedReader.readLine();
            int index = 0;
            if (line != null) {
                index = line.indexOf(": ");
                if (index != -1) {
                    cpuInfo[0] = line.substring(index + 2);
                }
            }

            // get cpu Hardware
            line = "";
            while (line != null && !line.contains("Hardware")) {
                line = bufferedReader.readLine();
            }
            if (line != null) {
                index = line.indexOf(": ");
                if (index != -1) {
                    cpuInfo[1] = line.substring(index + 2);
                }
            }
            bufferedReader.close();
        } catch (IOException ignored) {
        }
        return cpuInfo;
    }

    /**
     * 获取CPU核数
     *
     * @return cpu cores count
     */
    public static int getCpuCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Default to return 1 core
        return 1;
    }


    /**
     * 判断是否允许安装未知源软件
     *
     * @return boolean true 允许, false 不允许
     * @author Henry
     * @date 2015-3-30
     */
    public static boolean isAllowUnknownRes(Context context) {
        if (context == null) {
            return false;
        }
        boolean allow = false;
        try {
            float f = Settings.Secure.getFloat(context.getContentResolver(),
                    Settings.Secure.INSTALL_NON_MARKET_APPS);
            if (f == 1) {
                allow = true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return allow;
    }

    /**
     * 判断是否有SD卡
     *
     * @return boolean true 存在,false 不存在
     * @author Henry
     * @date 2015-3-30
     */
    public static boolean hasSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取当前语言
     *
     * @return String  such as CN, TW, US
     * @author Henry
     * @date 2015-3-30
     */
    public static String getSysLanguage() {
        return Locale.getDefault().getCountry();
    }

    /**
     * 获取SD卡剩余空间
     *
     * @return long
     * @author Henry
     * @date 2015-3-30
     */
    public static long getSDCardLeftSpace() {
        if (hasSDCard()) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            return blockSize * availCount;
        }
        return 0;
    }

    /**
     * 获取/DATA分区大小
     *
     * @return long
     * @author Henry
     * @date 2015-3-30
     */
    public static long getDataLeftSpace() {
        File root = Environment.getDataDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        return blockSize * availCount;
    }

    /**
     * 获取手机内部存储剩余空间
     *
     * @return long
     * @author Henry
     * @date 2015-3-30
     */
    public static long getSystemLeftSpace() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long availCount = sf.getAvailableBlocks();
        return blockSize * availCount;
    }

    /*
     *  获取手机内部存储空间
     *
     * @author Henry
     * @date 2014-5-21
     * @return long
     */
    public static long getSystemSpace() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        return blockSize * blockCount;
    }

    /**
     * 获取Android手机RAM大小
     *
     * @return 返回值的单位是M
     */
    public static long getTotalMemory() {
        String fileName = "/proc/meminfo";
        String totalMem = "";
        long totalRam = 1;
        BufferedReader localBufferedReader = null;
        try {
            FileReader fr = new FileReader(fileName);
            localBufferedReader = new BufferedReader(fr, 2048);
            if ((totalMem = localBufferedReader.readLine()) != null) {
                int begin = totalMem.indexOf(':');
                int end = totalMem.indexOf('k');
                // 采集数量的内存
                totalMem = totalMem.substring(begin + 1, end).trim();
                // 转换为Long型并将得到的内存单位转换为M
                long mega = 1024 * 1024;
                totalRam = Long.parseLong(totalMem) / mega;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != localBufferedReader) {
                try {
                    localBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                localBufferedReader = null;
            }
        }
        return totalRam;
    }

    /**
     * 获取Android手机可用RAM大小
     *
     * @return 返回值的单位是字节
     */
    public static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取系统内核版本
     *
     * @return String
     * @author Henry
     * @date 2015-3-30
     */
    public static String getKernelVersion() {
        String version = null;
        try {
            FileReader fileReader = new FileReader("/proc/version");
            BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
            String line = bufferedReader.readLine();
            String[] infos = line.split("\\s+");
            // KernelVersion
            version = infos[2];
            bufferedReader.close();
        } catch (IOException e) {
        }
        return version;
    }

    /**
     * 判断是否有静默安装权限
     *
     * @param context {@link Context}
     * @return boolean true 有 false 没有
     * @author Henry
     * @date 2015-3-30
     */
    public static boolean hasSilentInstallPermission(Context context) {
        if (context == null) {
            return false;
        }
        int id = context.checkCallingOrSelfPermission(android.Manifest.permission.INSTALL_PACKAGES);
        return (PackageManager.PERMISSION_GRANTED == id);
    }

    /**
     * 判断手机是否已经root
     *
     * @return boolean
     * @author Henry
     * @date 2015-3-30
     */
    public static boolean isRooted() {
        File suFile = new File("/system/bin/su");
        if (!suFile.exists()) {
            suFile = new File("/system/xbin/su");
            if (!suFile.exists()) {
                suFile = new File("/system/local/su");
            }
        }
        return suFile.exists();
    }


    /**
     * 取得网络类型，根据网络制式
     *
     * @param context
     * @return 返回分别是WIFI, 2G, 3G, 4G, UNKNOW
     */
    public static String getNetworkTypeName(Context context) {
        if (context == null) {
            return "UNKNOW";
        }
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = connManager.getActiveNetworkInfo();
        if (networkinfo == null) {
            return "UNKNOW";
        }
        int type = networkinfo.getType();
        if (type == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager teleMan = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int networkType = teleMan.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";

            }
        }
        return "UNKNOW";
    }

    /**
     * Get cpu usage
     *
     * @return return cpu usage
     */
    public static float cpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" +");  // Split on one or more spaces
            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
            Thread.sleep(360);
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0.0f;
    }

    /**
     * @return uuid
     */
    public static String getUdid(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * @param context
     * @return 国家
     */
    public static String getCountry(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();
    }

    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getAppVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getCarrier(Context context) {
        TelephonyManager telephonyManager
                = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperatorName();
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    /**
     * 是否在充电
     *
     * @param context
     * @return
     */
    public static boolean isCharging(Context context) {
        boolean isCharging = false;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        // 是否在充电
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
        }
        return isCharging;
    }
}
