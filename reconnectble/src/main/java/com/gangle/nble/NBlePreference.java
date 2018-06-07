package com.gangle.nble;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

class NBlePreference {

    private static final String PREFERENCE_SHARE = "preference.tggg.share";
    private static final String SERIALIZATION_LIST = "tggg.communication.storage.Preference.SERIALIZATION_LIST";
    private static Context mContext;
    public static SharedPreferences mPreferences;

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
        String json = new Gson().toJson(list);
        mPreferences.edit().putString(SERIALIZATION_LIST, json).commit();
    }

    public static List<String> restoreSerialization() {
        String serializations = mPreferences.getString(SERIALIZATION_LIST, "");
        List<String> list = new Gson().fromJson(serializations, new TypeToken<List<String>>() {
        }.getType());
        return list;
    }


}
