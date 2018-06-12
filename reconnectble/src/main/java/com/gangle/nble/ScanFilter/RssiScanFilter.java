package com.gangle.nble.ScanFilter;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Gang Tong on 2018/6/12.
 */
public class RssiScanFilter implements IScanFilter {
    private int minRssi;

    public RssiScanFilter(int minRssi) {
        this.minRssi = minRssi;
    }

    @Override
    public boolean isMatch(BluetoothDevice device, int rssi) {
        return rssi >= minRssi;
    }
}
