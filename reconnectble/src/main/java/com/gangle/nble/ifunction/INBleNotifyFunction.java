package com.gangle.nble.ifunction;

import android.content.Context;

import java.util.UUID;

public interface INBleNotifyFunction {

    /**
     * 获得设备所有的通知uuid
     */
    UUID[] getNotifyUuid();

    /**
     * 有设备发来的通知消息
     */
    void onNotify(Context context, String address, UUID uuid, byte[] value);

    /**
     * 读到的值
     */
    void onRead(Context context, String address, UUID uuid, byte[] value);

    /**
     * 已写成功的值
     */
    void onWrite(Context context, String address, UUID uuid, byte[] value);

    /**
     * service and chara discovered
     */
    void onServicesDiscovered(Context context, String address);

    /**
     * 开始连接，每次开始连接，都会调用此函数。
     * 接下去会调用connectGatt来开始尝试连接。
     * 如果connectGatt返回false，会直接触发onConnectFinish的调用
     */
    void onConnectStart(Context context, String address);

    /**
     * 连接中。connectGatt返回true，接下来是一个异步的结果回调。
     */
    void onConnecting(Context context, String address);

    /**
     * 连接成功。回调返回'连接成功'的结果
     */
    void onConnected(Context context, String address);

    /**
     * 连接断开。表示当onConnecting，或者onConnected后，连接的回调结果返回了错误。
     */
    void onDisconnected(Context context, String address);

    /**
     * 连接结束。可能是如果connectGatt返回false，也可能是连接的状态发生了改变。
     */
    void onConnectFinish(Context context, String address);

    /**
     * 设备Rssi值
     */
    void onRssi(Context context, String address, int rssi);

}
