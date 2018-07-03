package com.gangle.nble;

import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.gangle.nble.device.DeviceBase;
import com.gangle.nble.ifunction.INBleNotifyFunction;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Gang Tong
 * <p/>
 * 连接描述：ble设备的连接策略基本上是用户发起的连接，是'直接'连接。当直接连接不上返回exception后，并且是维护状态时，再次连接就采用'自动'连接。
 */
class NBleDeviceManagerImpl implements NBleDeviceManager, IDeviceConnectExceptionListener {

    /**
     * 记录的devices
     */
    private Map<String, NBleDevice> mDevices = Collections.synchronizedMap(new LinkedHashMap<String, NBleDevice>());
    private Set<NBleDevice> mMaintainSet = Collections.synchronizedSet(new HashSet<NBleDevice>());

    /**
     * 根据不同设备的notification的处理接口的列表，此表是根据设备名来区分。
     */
    private Map<String, INBleNotifyFunction> mNotifySubscription = Collections.synchronizedMap(new LinkedHashMap<String, INBleNotifyFunction>());

    /**
     * 默认的notification的处理接口。当在mNotifySubscription中没有找到对应设备的处理接口，则使用默认的。
     */
    private INBleNotifyFunction mDefaultSubscription;

    public OperationManager operationManager = new OperationManager();

    /**
     * 禁止外部新建实例
     */
    private NBleDeviceManagerImpl() {
        // prevent instantiation
    }

    private Context context;

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
     * 获取Context
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * 初始化
     */
    public void init(Context context) {
        this.context = context;

        // 第一次启动，恢复‘维护设备列表’。
        NBleDeviceManagerImpl.getInstance().restoreDevices(context);
    }

    /**
     * 创建device，INBleNotifyFunction表示后续
     */
    public NBleDevice createDevice(String address, String name) {
        assert getDevice(address) == null : String.format("The device with %s is EXIST!", address);
        NBleDevice device = new NBleDeviceImpl(NBleDeviceManagerImpl.getInstance().getContext(), address, name);
        NBleDeviceManagerImpl.getInstance().add(device);
        return device;
    }


    /**
     * 根据address来获取维护的device
     */
    public NBleDevice getDevice(String address) {
        return mDevices.get(address);
    }

    /**
     * 获取所有设备
     */
    public List<NBleDevice> getAllDevices() {
        ArrayList<NBleDevice> allDeviceSettingItems = new ArrayList<>(mDevices.values());
        return Collections.unmodifiableList(allDeviceSettingItems);
    }

    /**
     * 获取所有被维护的设备
     */
    public List<NBleDevice> getMaintainedDevices() {
        List<NBleDevice> items = new ArrayList<>();
        List<NBleDevice> allDeviceSettingItems = new ArrayList<>(mDevices.values());
        for (NBleDevice device : allDeviceSettingItems) {
            if (NBle.manager().isMaintain(device)) {
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
     * 查询某设备是否是维护状态
     */
    public synchronized boolean isMaintain(NBleDevice device) {
        return device != null && mMaintainSet.contains(device);
    }

    /**
     * 根据device设置设备的维护状态
     */
    public synchronized void setMaintain(NBleDevice device, boolean bMaintain) {
        if (bMaintain) {
            mMaintainSet.add(device);
        } else {
            mMaintainSet.remove(device);
        }
        NBleDeviceManagerImpl.getInstance().storeDevices();
    }

    /**
     * 根据设备名获取notification的接口
     */
    public synchronized INBleNotifyFunction getNotification(String deviceName) {
        INBleNotifyFunction ifunction = mNotifySubscription.get(deviceName);
        return ifunction == null ? mDefaultSubscription : ifunction;
    }

    /**
     * 根据设备名注册notification的处理接口
     */
    public synchronized void registerNotification(String deviceName, INBleNotifyFunction iFunction) {
        if (!mNotifySubscription.containsKey(deviceName)) {
            mNotifySubscription.put(deviceName, iFunction);
        }
    }

    /**
     * 注册notification的默认处理接口
     */
    public void registerDefaultNotification(INBleNotifyFunction iFunction) {
        mDefaultSubscription = iFunction;
    }

    /**
     * 添加设备
     */
    public synchronized void add(NBleDevice deviceSettingItem, boolean store) {
        mDevices.put(deviceSettingItem.getAddress(), deviceSettingItem);
        if (store)
            storeDevices();
    }

    public synchronized void add(NBleDevice deviceSettingItem) {
        add(deviceSettingItem, false);
    }

    /**
     * 删除设备
     */
    public synchronized void remove(NBleDevice device) {
        LogUtils.v("remove Device:%s", device.getAddress());
        NBleDeviceImpl remove = (NBleDeviceImpl) mDevices.remove(device.getAddress());
        if (remove != null && isMaintain(remove)) {
            storeDevices();
        }
    }

    /**
     * 断开设备
     */
    public void disconnect(NBleDevice bleDevice) {
        ((NBleDeviceImpl) bleDevice).disconnectImpl();
        // 在连接过程中做disconnect，会导致连接中断，且没有回调。
        // 所以每次重连需要先做close，以及后续的判断处理。
        reconnect(bleDevice);
    }

    /**
     * 序列化设备。只序列化设为“维护”的设备。
     */
    public void storeDevices() {
        LogUtils.v("Store Device size:%d", mMaintainSet.size());
        synchronized (mMaintainSet) {
            List<DeviceBase> list = new ArrayList<>();
            for (NBleDevice device : mMaintainSet) {
                list.add((DeviceBase) device);
            }
            NBlePreference.getInstance().saveSerialization(list);
        }
    }

    /**
     * 反序列化设备
     */
    public void restoreDevices(Context context) {
        List<DeviceBase> serializationList = NBlePreference.getInstance().restoreSerialization();
        if (serializationList != null) {
            synchronized (mDevices) {
                for (DeviceBase deviceBase : serializationList) {
                    NBleDevice device = getDevice(deviceBase.getAddress());
                    if (device == null) {
                        device = createDevice(deviceBase.getAddress(), deviceBase.getName());
                    }
                    setMaintain(device, true);
                    LogUtils.v("Restore Device:%s, isMaintain:%s", deviceBase.getAddress(), true);
                }
            }
        }
    }

    /**
     * 在连接过程中做disconnect，会导致连接中断，且没有回调。
     * 所以每次重连需要先做close，以及后续的判断处理。
     */
    protected void reconnect(final NBleDevice device) {
        Observable observable = Observable.just(device)
                .subscribeOn(Schedulers.newThread())
                .filter(new Predicate<NBleDevice>() {
                    @Override
                    public boolean test(NBleDevice device) throws Exception {
                        return (device.getConnectionState() != BluetoothProfile.STATE_CONNECTED)
                                && (device.getConnectionState() != BluetoothProfile.STATE_CONNECTING);
                    }
                })
                .map(new Function<NBleDevice, NBleDevice>() {
                    @Override
                    public NBleDevice apply(NBleDevice device) throws Exception {
                        ((NBleDeviceImpl) device).close();
                        return device;
                    }
                })
                .filter(new Predicate<NBleDevice>() {
                    @Override
                    public boolean test(NBleDevice device) throws Exception {
                        return NBleUtil.isAdapterEnable(context) && NBleDeviceManagerImpl.getInstance().isMaintain(device);
                    }
                })
                .delay(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .map(new Function<NBleDevice, Boolean>() {
                    @Override
                    public Boolean apply(NBleDevice device) throws Exception {
                        return ((NBleDeviceImpl) device).connectDirectly();
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        reconnect(device);
                    }
                });

        operationManager.pend(observable);
//                .subscribe(new Consumer<Boolean>() {
//                    @Override
//                    public void accept(Boolean aBoolean) throws Exception {
//
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) throws Exception {
//                        reconnect(device);
//                    }
//                }, new Action() {
//                    @Override
//                    public void run() throws Exception {
//
//                    }
//                });

    }


    /**
     * device 断开异常的监听，用来做后续的处理。
     *
     * @param device
     * @param e
     */
    @Override
    public void onDisconnected(NBleDevice device, Exception e) {
        try {
            if (isMaintain(device)) {
                if (e == null) {
                    // 如果正常断开，则可以尝试自动连接。
                    Preconditions.checkState(((NBleDeviceImpl) device).connectAuto(), "gatt.connectImpl() fail");
                } else {
                    // 如果异常断开，则走重连的过程。
                    reconnect(device);
                }
            }
        } catch (Exception exception) {
            reconnect(device);
        }


    }
}
