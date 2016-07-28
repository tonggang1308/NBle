package com.tggg.nble;

public interface NBleScanner {

    /**
     * start to scan
     */
    boolean start(BleScanListener callback);

    /**
     * stop the scan
     */
    void stop();

    /**
     * is scanning
     */
    boolean isScanning();

    interface BleScanListener {
        void onScanStarted();

        void onScanStopped();

        void onDeviceDiscovered(String address, String name, int rssi, byte[] scanRecord);
    }

}
