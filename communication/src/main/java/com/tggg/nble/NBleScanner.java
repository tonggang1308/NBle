package com.tggg.nble;

public interface NBleScanner {

    boolean start(BleScanListener callback);

    void stop();

    boolean isScanning();

    interface BleScanListener {
        void onScanStarted();

        void onScanStopped();

        void onDeviceDiscovered(String address, String name, int rssi, byte[] scanRecord);
    }

}
