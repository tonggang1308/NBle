package xyz.gangle.bleconnector.app;

import android.app.Application;
import android.content.Context;

import com.gangle.nble.NBle;
import com.gangle.nble.NBlePreference;
import com.gangle.nble.device.DeviceBase;
import com.gangle.util.PreferenceUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import timber.log.Timber;
import xyz.gangle.bleconnector.BuildConfig;
import xyz.gangle.bleconnector.data.BaseBleNotifyFunction;
import xyz.gangle.bleconnector.preference.SharedPrefManager;

public class ConnectorApplication extends Application {

    private static ConnectorApplication INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();

        INSTANCE = this;

        //Log util
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.i("ConnectorApplication onCreate.");

        // 初始化存储
        SharedPrefManager.getInstance().init(this);

        // 初始化NBle
        NBle.init(this, new NBlePreference.DeviceSerialization() {
            @Override
            public void saveSerialization(List<DeviceBase> list) {
                SharedPrefManager.getInstance().saveSerialization(list);
            }

            @Override
            public List<DeviceBase> restoreSerialization() {
                return SharedPrefManager.getInstance().restoreSerialization();
            }
        });

        // 注册响应
        NBle.manager().registerDefaultNotification(new BaseBleNotifyFunction());


        // LeanCloud 初始化
//        AVOSCloud.initialize(this, "UtsxPjcH3tf1iF7J7X8UWLRe-gzGzoHsz", "WNsrgzJN9sD22JhYIHdCfjyA");

    }

    public static ConnectorApplication getInstance() {
        return INSTANCE;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }
}
