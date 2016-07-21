package com.tggg.nble;

import com.tggg.nble.ifunction.IBleNotifyFunction;

import java.util.List;

/**
 * Created by Gang Tong
 */
public interface NBleDeviceManager {

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
     * 设置某address的设备是否是维护状态
     */
    void setMaintain(String address, boolean bMaintain);

    /**
     * 根据设备名获取notification的接口
     */
    IBleNotifyFunction getNotification(String deviceName);

    /**
     * 根据设备名注册notification的处理接口
     */
    void registerNotification(String deviceName, IBleNotifyFunction iFunction);

    /**
     * 注册notification的默认处理接口
     */
    void registerDefaultNotification(IBleNotifyFunction iFunction);

    /**
     * 删除设备
     */
    void remove(String address);

}
