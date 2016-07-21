package com.tggg.nble;

import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.tggg.nble.ifunction.IBleNotifyFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Gang Tong
 */
class NBleDeviceManagerImpl implements NBleDeviceManager {

    /**
     * 记录的devices
     */
    private Map<String, NBleDevice> mDevices = Collections.synchronizedMap(new LinkedHashMap<String, NBleDevice>());

    /**
     * 根据不同设备的notification的处理接口的列表，此表是根据设备名来区分。
     */
    private Map<String, IBleNotifyFunction> mNotfitySubscrip = Collections.synchronizedMap(new LinkedHashMap<String, IBleNotifyFunction>());

    /**
     * 默认的notification的处理接口。当在mNotifysubscrip中没有找到对应设备的处理接口，则使用默认的。
     */
    private IBleNotifyFunction mDefaultSubscrip;

    /**
     * 禁止外部新建实例
     */
    private NBleDeviceManagerImpl() {
        // prevent instantiation
    }

    /**
     * 单例
     */
    private static class LazyHolder {
        private static final NBleDeviceManagerImpl INSTANCE = new NBleDeviceManagerImpl();
    }

    public static NBleDeviceManagerImpl getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * 根据address来获取维护的device
     */
    public NBleDevice getDevice(String address) {
        return mDevices.get(address);
    }

    /**
     * 获取所有维护设备的address列表
     */
    public List<String> getAllAddresses() {
        ArrayList<String> allAddresses = new ArrayList<>(mDevices.keySet());
        return Collections.unmodifiableList(allAddresses);
    }

    /**
     * 获取所有设备
     */
    public List<NBleDevice> getAllDevices() {
        ArrayList<NBleDevice> allDeviceSettingItems = new ArrayList<>(mDevices.values());
        return Collections.unmodifiableList(allDeviceSettingItems);
    }

    /**
     * 获取所有设备
     *
     * @param maintain 是否是维护状态的
     */
    public List<NBleDevice> getAllDevices(boolean maintain) {
        List<NBleDevice> items = new ArrayList<>();
        List<NBleDevice> allDeviceSettingItems = new ArrayList<>(mDevices.values());
        for (NBleDevice device : allDeviceSettingItems) {
            if (((NBleDeviceImpl) device).isMaintain() == maintain) {
                items.add(device);
            }
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * 获取所有已连接的设备
     */
    public List<NBleDevice> getConnectedDevices() {
        List<NBleDevice> items = new ArrayList<>();
        List<NBleDevice> allDeviceSettingItems = new ArrayList<>(mDevices.values());
        for (NBleDevice device : allDeviceSettingItems) {
            int state = ((NBleDeviceImpl) device).getConnectionState();
            if (state == BluetoothProfile.STATE_CONNECTED) {
                items.add(device);
            }
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * 查询某address的设备是否是维护状态
     */
    public synchronized boolean isMaintain(String address) {
        NBleDeviceImpl device = (NBleDeviceImpl) getDevice(address);
        return device != null && device.isMaintain();
    }

    public synchronized void setMaintain(String address, boolean bMaintain) {
        NBleDeviceImpl device = (NBleDeviceImpl) getDevice(address);
        device.setMaintain(bMaintain);
        NBleDeviceManagerImpl.getInstance().storeDevices();
    }


    /**
     * 根据设备名获取notification的接口
     */
    public synchronized IBleNotifyFunction getNotification(String deviceName) {
        IBleNotifyFunction ifunction = mNotfitySubscrip.get(deviceName);
        return ifunction == null ? mDefaultSubscrip : ifunction;
    }

    /**
     * 根据设备名注册notification的处理接口
     */
    public synchronized void registerNotification(String deviceName, IBleNotifyFunction iFunction) {
        if (!mNotfitySubscrip.containsKey(deviceName)) {
            mNotfitySubscrip.put(deviceName, iFunction);
        }
    }

    /**
     * 注册notification的默认处理接口
     */
    public void registerDefaultNotification(IBleNotifyFunction iFunction) {
        mDefaultSubscrip = iFunction;
    }


    /**
     * 添加设备
     */
    public synchronized void add(NBleDevice deviceSettingItem) {
        mDevices.put(deviceSettingItem.getAddress(), (NBleDeviceImpl) deviceSettingItem);
        storeDevices();
    }


    /**
     * 删除设备
     */
    public synchronized void remove(String address) {
        Timber.v("remove Device:%s", address);
        NBleDeviceImpl remove = (NBleDeviceImpl) mDevices.remove(address);
        if (remove != null && remove.isMaintain()) {
            storeDevices();
        }
    }


    /**
     * 序列化设备。只序列化设为“维护”的设备。
     */
    public void storeDevices() {
        Timber.v("Store Device size:%d", mDevices.size());
        List<String> serializationList = new ArrayList<String>();
        synchronized (mDevices) {
            for (NBleDevice device : mDevices.values()) {
                if (((NBleDeviceImpl) device).isMaintain()) {
                    Timber.v("Store Device:%s", device.getAddress());
                    serializationList.add(((NBleDeviceImpl) device).serialize());
                }
            }
        }
        NBlePreference.saveSerialization(serializationList);
    }

    /**
     * 反序列化设备
     */
    public void restoreDevices(Context context) {
        List<String> serializationList = NBlePreference.restoreSerialization();
        if (serializationList != null) {
            synchronized (mDevices) {
                for (String serialization : serializationList) {
                    NBleDeviceImpl device = NBleDeviceImpl.deserialize(context, serialization);
                    Timber.v("Restore Device:%s", device.getAddress());
                    if (device != null) {
                        setMaintain(device.getAddress(), true);
                    }
                }
            }
        }
    }
}
