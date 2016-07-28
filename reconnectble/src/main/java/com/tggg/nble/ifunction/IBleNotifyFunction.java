package com.tggg.nble.ifunction;

import android.content.Context;

import java.util.UUID;

public interface IBleNotifyFunction {

    UUID[] getNotifyUuid();

    void onNotify(Context context, String address, UUID uuid, byte[] value);

    void onRead(Context context, String address, UUID uuid, byte[] value);

    void onWrite(Context context, String address, UUID uuid, byte[] value);

    void onConnected(Context context, String address);

    void onConnecting(Context context, String address);

    void onDisConnected(Context context, String address);

    void onClose(Context context, String address);

    void onRssi(Context context, String address, int rssi);

}
