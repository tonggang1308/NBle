package com.gangle.nble;

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
     * filter by name, default care case
     *
     * @param scanName null:disable
     */
    void setScanName(String scanName);

    /**
     * filter by names, default care case
     */
    void setScanNames(String[] scanNames);

    /**
     * ignore scan name case
     *
     * @param ignoreCase default: false
     */
    void setNameCaseIgnore(boolean ignoreCase);


    /**
     * ignore unknown device
     *
     * @param ignoreUnknown
     */
    void setUnknownDeviceIgnore(boolean ignoreUnknown);

    /**
     * filter by rssi
     *
     * @param rssi: 0 ~ -100, null:disable
     */
    void setRssiLimit(Integer rssi);

    /**
     * filter by mac range
     *
     * @param start Start from
     * @param end   End to
     *              如果start值大于end值，则表示range是跨越FF:FF:FF:FF:FF:FF和00:00:00:00:00:00
     */
    boolean setMacRange(String start, String end);

    /**
     * filter by mac range
     */
    void setMac(String mac);


    interface BleScanListener {
        void onScanStarted();

        void onScanStopped();

        void onDeviceDiscovered(String address, String name, int rssi, byte[] scanRecord);
    }

}
