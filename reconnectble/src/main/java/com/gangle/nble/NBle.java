package com.gangle.nble;

import android.content.Context;

import java.util.UUID;

/**
 * Created by Gang Tong
 */
public final class NBle {
    /**
     * 扫描
     */
    static private NBleScannerImpl scanner;

    private NBle() {
    }

    static public void init(Context context) {
        init(context);
    }

    static public void init(Context context, NBlePreference.DeviceSerialization serialization) {
        // BLE Share Preference
        NBlePreference.init(context, serialization);

        // BLE Manager
        NBleDeviceManagerImpl.getInstance().init(context);

        // BLE Service
        NBleService.start(context);

        // Operation Manager
        OperationManager.getInstance().init(NBleDeviceManagerImpl.getInstance());

        // Scanner
        scanner = new NBleScannerImpl(context);
    }

    static public NBleDeviceManager manager() {
        return NBleDeviceManagerImpl.getInstance();
    }

    static public NBlePreference getPreference() {
        return NBlePreference.getInstance();
    }


    /**
     * 获取scanner
     */
    static public NBleScanner getScanner() {
        return scanner;
    }


//    interface NBleSerializeInterface {
//        NBleDevice onDeviceCreate();
//    }


//    /**
//     * BLE Device Builder
//     */
//    public static class DeviceBuilder {
//        private NBleDeviceImpl nBleDevice;
//
//        private DeviceBuilder() {
//        }
//
//        public DeviceBuilder(String address, String name) {
//            this.nBleDevice = new NBleDeviceImpl(NBleDeviceManagerImpl.getInstance().getContext(), address, name);
//        }
//
//        public DeviceBuilder setMaintain(boolean isMaintain) {
//            this.nBleDevice.setMaintain(isMaintain);
//            return this;
//        }
//
//        public DeviceBuilder setINotifyFunction(INBleNotifyFunction iNBleNotifyFunction) {
//            this.nBleDevice.setiNotifyFunction(iNBleNotifyFunction);
//            return this;
//        }
//
//        public NBleDevice build() {
//            if (this.nBleDevice.getNotifyFunction() == null) {
//                // 根据设备名获取notify function
//                INBleNotifyFunction iNBleNotifyFunction = NBleDeviceManagerImpl.getInstance().getNotification(nBleDevice.getName());
//                setINotifyFunction(iNBleNotifyFunction);
//                LogUtils.d("Builder set iNBleNotifyFunction: %s", iNBleNotifyFunction == null ? "null" : iNBleNotifyFunction.getClass().getName());
//            }
//            NBleDeviceManagerImpl.getInstance().add(this.nBleDevice);
//            return this.nBleDevice;
//        }
//    }
}
