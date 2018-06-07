package com.gangle.nble;

import android.app.Service;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import timber.log.Timber;

/**
 * Created by Gang Tong.
 */
public class NBleService extends Service {
    private static final String ACTION_REASON = "START_REASON";

    private static final int REASON_NORMAL = 0x00;
    private static final int REASON_ADAPTER_TURN_OFF = 0x01;
    private static final int REASON_ADAPTER_TURN_ON = 0x02;


    public static void start(Context context) {
        Intent intent = new Intent(context, NBleService.class);
        intent.putExtra(ACTION_REASON, REASON_NORMAL);
        context.startService(intent);
    }

    public static void startAdapterTurnOff(Context context) {
        Intent intent = new Intent(context, NBleService.class);
        intent.putExtra(ACTION_REASON, REASON_ADAPTER_TURN_OFF);
        context.startService(intent);
    }

    public static void startAdapterTurnOn(Context context) {
        Intent intent = new Intent(context, NBleService.class);
        intent.putExtra(ACTION_REASON, REASON_ADAPTER_TURN_ON);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate, first start NBleService");

        // 第一次启动，恢复‘维护设备列表’。
        if (NBleUtil.isAdapterEnable(this)) {
            // 重新连接‘需要维护的设备’
            reconnectAll();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Timber.i("onStartCommand, flags:%d, startId:%d, reason:%s", flags, startId, intent == null ? " Restart after be Removed." : intent.getIntExtra(ACTION_REASON, REASON_NORMAL));

        if (intent != null) {
            switch (intent.getIntExtra(ACTION_REASON, REASON_NORMAL)) {
                case REASON_NORMAL:
                    break;
                case REASON_ADAPTER_TURN_OFF:
                    // 每个已连接蓝牙设备都会收到disconnected的消息，会自己处理disconnect和close。
                    // 但出于connecting，未必会收到，所以需要close all。
                    closeAll();
                    break;
                case REASON_ADAPTER_TURN_ON:
                    // 重新连接
                    reconnectAll();
                    break;
            }
        }
        return START_STICKY;
    }

    private void closeAll() {
        Timber.i("service closeAll()");
        for (NBleDevice device : NBleDeviceManagerImpl.getInstance().getAllDevices()) {
            device.disconnect();
        }
    }

    private void reconnectAll() {
        Timber.i("service reconnectAll()");
        if (NBleUtil.isAdapterEnable(this)) {
            for (NBleDevice device : NBleDeviceManagerImpl.getInstance().getAllDevices()) {
                if (device.getConnectionState() == BluetoothProfile.STATE_DISCONNECTED && device.isMaintain()) {
                    device.connect();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.w("onDestroy()");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Timber.w("onTaskRemoved()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
