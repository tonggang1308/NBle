package com.gangle.nble;

import com.gangle.nble.ifunction.INBleNotifyFunction;

import java.util.List;

/**
 * Created by Gang Tong
 */
public interface NBleDeviceManager {

    /**
     * 创建device，INBleNotifyFunction表示后续
     */
    NBleDevice createDevice(String address, String name);

    /**
     * 根据address来获取维护的device
     */
    NBleDevice getDevice(String address);

    /**
     * 获取所有设备
     */
    List<NBleDevice> getAllDevices();

    /**
     * 获取所有设备
     *
     * @param maintain 是否是维护状态的
     */
    List<NBleDevice> getAllDevices(boolean maintain);

    /**
     * 获取所有已连接的设备
     */
    List<NBleDevice> getConnectedDevices();

    /**
     * 查询某address的设备是否是维护状态
     */
    boolean isMaintain(String address);

    /**
     * 查询某设备是否是维护状态
     */
    boolean isMaintain(NBleDevice device);

    /**
     * 根据device设置设备的维护状态
     */
    void setMaintain(NBleDevice device, boolean bMaintain);

    /**
     * 设置某address的设备是否是维护状态
     */
    void setMaintain(String address, boolean bMaintain);

    /**
     * 根据设备名获取notification的接口
     */
    INBleNotifyFunction getNotification(String deviceName);

    /**
     * 根据设备名注册notification的处理接口
     */
    void registerNotification(String deviceName, INBleNotifyFunction iFunction);

    /**
     * 注册notification的默认处理接口
     */
    void registerDefaultNotification(INBleNotifyFunction iFunction);

    /**
     * 删除设备
     */
    void remove(String address);

}
