package com.gangle.nble;

import android.content.Context;

import com.gangle.nble.ifunction.IBleNotifyFunction;

import java.util.UUID;

import timber.log.Timber;

/**
 * Created by Gang Tong
 */
public final class NBle {
    private NBle() {
    }

    static public NBleDeviceManager getManager() {
        return NBleDeviceManagerImpl.getInstance();
    }

    static public void init(Context context) {
        // BLE Share Preference
        NBlePreference.init(context);

        // BLE Manager
        NBleDeviceManagerImpl.getInstance().init(context);

        // BLE Service
        NBleService.start(context);

        // Operation Manager
        OperationManager.getInstance().init(NBleDeviceManagerImpl.getInstance());

    }

    public static class ScannerBuilder {
        NBleScannerImpl nBleScanner;

        public ScannerBuilder(Context context) {
            nBleScanner = new NBleScannerImpl(context);
        }

        public ScannerBuilder setUuids(UUID[] uuids) {
            nBleScanner.setUuids(uuids);
            return this;
        }

        public NBleScanner build() {
            return nBleScanner;
        }
    }

    /**
     * BLE Device Builder
     */
    public static class DeviceBuilder {
        private NBleDeviceImpl nBleDevice;

        private DeviceBuilder() {
        }

        public DeviceBuilder(String address, String name) {
            this.nBleDevice = new NBleDeviceImpl(NBleDeviceManagerImpl.getInstance().getContext(), address, name);
        }

        public DeviceBuilder setMaintain(boolean isMaintain) {
            this.nBleDevice.setMaintain(isMaintain);
            return this;
        }

        public DeviceBuilder setINotifyFunction(IBleNotifyFunction iBleNotifyFunction) {
            this.nBleDevice.setiNotifyFunction(iBleNotifyFunction);
            return this;
        }

        public NBleDevice build() {
            if (this.nBleDevice.getNotifyFunction() == null) {
                // 根据设备名获取notify function
                IBleNotifyFunction iBleNotifyFunction = NBleDeviceManagerImpl.getInstance().getNotification(nBleDevice.getName());
                setINotifyFunction(iBleNotifyFunction);
                Timber.d("Builder set iBleNotifyFunction: %s", iBleNotifyFunction == null ? "null" : iBleNotifyFunction.getClass().getName());
            }
            NBleDeviceManagerImpl.getInstance().add(this.nBleDevice);
            return this.nBleDevice;
        }
    }
}
