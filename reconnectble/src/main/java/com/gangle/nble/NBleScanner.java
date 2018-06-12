package com.gangle.nble;

import com.gangle.nble.ScanFilter.IScanFilter;

public interface NBleScanner {
    int INDEFINITE = 0;

    /**
     * start to scan
     */
    boolean start(BleScanListener callback, int duration);

    boolean start(BleScanListener callback);

    /**
     * stop the scan
     */
    void stop();

    /**
     * is scanning
     */
    boolean isScanning();

    /**
     * set filters
     * @param filters
     */
    void setFilters(IScanFilter[] filters);

    interface BleScanListener {
        void onScanStarted();

        void onScanStopped();

        void onDeviceDiscovered(String address, String name, int rssi, byte[] scanRecord);
    }

}
