package xyz.gangle.bleconnector.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.tggg.util.PreferenceUtil;


public class SharedPrefManager {
    SharedPreferences sharedPreferences;
    /**
     * 扫描的方式
     * 0:表示自动连续扫描, >0 表示手动扫描
     */
    public static final String KEY_SCAN_MODE = "KEY_SCAN_MODE";

    /**
     * 扫描的时间
     */
    public static final String KEY_SCAN_PERIOD = "KEY_SCAN_PERIOD";

    private SharedPrefManager() {
    }

    public static SharedPrefManager getInstance() {
        return SharedPrefManager.LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final SharedPrefManager INSTANCE = new SharedPrefManager();
    }

    public void init(Context context) {
        sharedPreferences = PreferenceUtil.getDefaultSharedPreference(context);
    }

    /**
     * 扫描方式
     */
    public int getScanMode() {
        return PreferenceUtil.readInt(sharedPreferences, KEY_SCAN_MODE);
    }

    public void setScanMode(int mode) {
        PreferenceUtil.write(sharedPreferences, KEY_SCAN_MODE, mode);
    }

    /**
     * 扫描时间
     */
    public int getScanPeriod() {
        return PreferenceUtil.readInt(sharedPreferences, KEY_SCAN_PERIOD, 1);
    }

    public void setScanPeriod(int period) {
        PreferenceUtil.write(sharedPreferences, KEY_SCAN_PERIOD, period);
    }

}
