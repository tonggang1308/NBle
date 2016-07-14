package xyz.gangle.bleconnector.app;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;
import xyz.gangle.bleconnector.BuildConfig;
import xyz.gangle.bleconnector.data.BaseBleNotifyFunction;

import com.tggg.nble.NBle;
import com.tggg.nble.NBleDeviceManager;

/**
 * Created by yiyidu on 5/28/16.
 */

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
        NBleDeviceManager.getInstance().registerDefaultNotification(new BaseBleNotifyFunction());



    }

    public static ConnectorApplication getInstance() {
        return INSTANCE;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }
}
