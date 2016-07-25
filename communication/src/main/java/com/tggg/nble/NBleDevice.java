package com.tggg.nble;

import android.bluetooth.BluetoothDevice;

import com.tggg.nble.Record.StatusChangeRecord;
import com.tggg.nble.ifunction.IBleNotifyFunction;

import java.util.List;
import java.util.UUID;

/**
 * Created by Gang Tong.
 */
public interface NBleDevice {

    String getAddress();

    /**
     * 获取设备名称
     */
    String getName();

    /**
     * 判断是否是维护状态
     */
    boolean isMaintain();

    /**
     * 设置维护状态
     */
    void setMaintain(boolean maintain);

    /**
     * 获取状态记录
     */
    List<StatusChangeRecord> getStatusRecordList();

    /**
     * 获取device
     */
    BluetoothDevice getBleDevice();

    /**
     * 写数据
     */
    void write(UUID serviceUuid, UUID characteristicUuid, byte[] data);

    /**
     * 读数据
     */
    void read(UUID serviceUuid, UUID characteristicUuid);

    /**
     * 请求Rssi值。
     */
    void requestRemoteRssi();

    /**
     * 获取当前设备的连接状态
     */
    int getConnectionState();

    /**
     * 断开连接。
     */
    void disconnect();

    /**
     * Connect to the device directly
     */
    boolean connect();

}
