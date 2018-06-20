package com.gangle.nble;

import android.content.Context;
import android.content.SharedPreferences;

import com.gangle.nble.ScanFilter.IScanFilter;
import com.gangle.nble.device.DeviceBase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NBlePreference {

    private static final String PREFERENCE_SHARE = "preference.tggg.share";
    private static final String SERIALIZATION_LIST = "tggg.communication.storage.Preference.SERIALIZATION_LIST";
    private static Context mContext;
    public static SharedPreferences mPreferences;
    private static DeviceSerialization mSerialization;

    private NBlePreference() {
    }

    private static class LazyHolder {
        private static final NBlePreference INSTANCE = new NBlePreference();
    }

    public static NBlePreference getInstance() {
        return LazyHolder.INSTANCE;
    }

    static void init(Context context, DeviceSerialization serialization) {
        mContext = context;
        mPreferences = mContext.getSharedPreferences(PREFERENCE_SHARE, Context.MODE_MULTI_PROCESS);
        mSerialization = serialization;
    }

    void cleanPreference() {
        mPreferences.edit().clear().commit();
    }

    void saveSerialization(List<DeviceBase> list) {
        if (mSerialization != null) {
            List<DeviceBase> serialList = Collections.synchronizedList(new ArrayList<DeviceBase>());
            for (DeviceBase device : list) {
                serialList.add(new DeviceBase(device.getAddress(), device.getName()));
            }
            mSerialization.saveSerialization(serialList);
        }
    }

    List<DeviceBase> restoreSerialization() {
        if (mSerialization != null)
            return mSerialization.restoreSerialization();
        else
            return null;
    }

    public interface DeviceSerialization {
        void saveSerialization(List<DeviceBase> list);

        List<DeviceBase> restoreSerialization();
    }

}
