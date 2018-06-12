package com.gangle.nble.ScanFilter;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Gang Tong on 2018/6/12.
 */
public interface IScanFilter {
    boolean isMatch(BluetoothDevice device, final int rssi);
}


