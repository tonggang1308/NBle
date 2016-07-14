package com.tggg.nble;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;

class NBlePreference {

    private static final String PREFERENCE_SHARE = "preference.tggg.share";
    static private final String SERIALIZATION_LIST = "tggg.communication.storage.Preference.SERIALIZATION_LIST";
    static private Context mContext;
    static public SharedPreferences mPreferences;

    private NBlePreference() {
    }

    private static class LazyHolder {
        private static final NBlePreference INSTANCE = new NBlePreference();
    }

    public static NBlePreference getInstance() {
        return LazyHolder.INSTANCE;
    }

    public static void init(Context context) {
        mContext = context;
        mPreferences = mContext.getSharedPreferences(PREFERENCE_SHARE, Context.MODE_MULTI_PROCESS);
    }

    public void cleanPreference() {
        mPreferences.edit().clear().commit();
    }

    public static void saveSerialization(List<String> list) {
        String json = JSON.toJSONString(list, SerializerFeature.WriteClassName);
        mPreferences.edit().putString(SERIALIZATION_LIST, json).commit();
    }

    public static List<String> restoreSerialization() {
        String serializations = mPreferences.getString(SERIALIZATION_LIST, "");
        List<String> list = (List<String>) JSON.parse(serializations);
        return list;
    }


}
