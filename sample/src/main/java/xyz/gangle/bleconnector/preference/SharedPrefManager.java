package xyz.gangle.bleconnector.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.gangle.util.PreferenceUtil;


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
    public static final String KEY_SCAN_DURATION = "KEY_SCAN_DURATION";

    /**
     * filter NAME
     */
    public static final String KEY_FILTER_NAME_ENABLE = "KEY_FILTER_NAME_ENABLE";
    public static final String KEY_FILTER_NAME_VALUE = "KEY_FILTER_NAME_VALUE";

    /**
     * filter MAC
     */
    public static final String KEY_FILTER_MAC_ENABLE = "KEY_FILTER_MAC_ENABLE";
    public static final String KEY_FILTER_MAC_VALUE = "KEY_FILTER_MAC_VALUE";

    /**
     * filter MAC SCOPE
     */
    public static final String KEY_FILTER_MAC_SCOPE_ENABLE = "KEY_FILTER_MAC_SCOPE_ENABLE";
    public static final String KEY_FILTER_MAC_START_VALUE = "KEY_FILTER_MAC_START_VALUE";
    public static final String KEY_FILTER_MAC_END_VALUE = "KEY_FILTER_MAC_END_VALUE";

    /**
     * filter RSSI
     */
    public static final String KEY_FILTER_RSSI_ENABLE = "KEY_FILTER_RSSI_ENABLE";
    public static final String KEY_FILTER_RSSI_VALUE = "KEY_FILTER_RSSI_VALUE";

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
    public int getScanDuration() {
        return PreferenceUtil.readInt(sharedPreferences, KEY_SCAN_DURATION, 1);
    }

    public void setScanDuration(int duration) {
        PreferenceUtil.write(sharedPreferences, KEY_SCAN_DURATION, duration);
    }


    /**
     * Filter Enable
     */
    public boolean isFilterEnable(String key) {
        return PreferenceUtil.readBoolean(sharedPreferences, key);
    }

    public void setFilterEnable(String key, boolean enable) {
        PreferenceUtil.write(sharedPreferences, key, enable);
    }

    /**
     * Filter Name
     */
    public String getFilterName() {
        return PreferenceUtil.readString(sharedPreferences, KEY_FILTER_NAME_VALUE, "");
    }

    public void setFilterName(String name) {
        PreferenceUtil.write(sharedPreferences, KEY_FILTER_NAME_VALUE, name);
    }


    /**
     * Filter Mac
     */
    public String getFilterMac() {
        return PreferenceUtil.readString(sharedPreferences, KEY_FILTER_MAC_VALUE, "");
    }

    public void setFilterMac(String mac) {
        PreferenceUtil.write(sharedPreferences, KEY_FILTER_MAC_VALUE, mac);
    }

    /**
     * Filter Mac Scope
     */
    public String getFilterMacStart() {
        return PreferenceUtil.readString(sharedPreferences, KEY_FILTER_MAC_START_VALUE, "");
    }

    public void setFilterMacStart(String mac) {
        PreferenceUtil.write(sharedPreferences, KEY_FILTER_MAC_START_VALUE, mac);
    }

    public String getFilterMacEnd() {
        return PreferenceUtil.readString(sharedPreferences, KEY_FILTER_MAC_END_VALUE, "");
    }

    public void setFilterMacEnd(String mac) {
        PreferenceUtil.write(sharedPreferences, KEY_FILTER_MAC_END_VALUE, mac);
    }

    /**
     * Filter RSSI
     */
    public int getFilterRssi() {
        return PreferenceUtil.readInt(sharedPreferences, KEY_FILTER_RSSI_VALUE, -99);
    }

    public void setFilterRssi(int rssi) {
        PreferenceUtil.write(sharedPreferences, KEY_FILTER_RSSI_VALUE, rssi);
    }

}
