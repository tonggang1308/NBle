package xyz.gangle.bleconnector.app;

import android.app.Application;
import android.content.Context;

import com.tggg.nble.NBle;

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

        NBle.init(this);

        // 注册响应
        NBle.getManager().registerDefaultNotification(new BaseBleNotifyFunction());

        // 初始化存储
        SharedPrefManager.getInstance().init(this);

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
