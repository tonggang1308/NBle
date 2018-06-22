package com.gangle.nble.ScanFilter;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created by Gang Tong on 2018/6/12.
 */
public class NameScanFilter implements IScanFilter {
    private String[] namesFilter;
    private boolean ignoreUnknown = false;
    private boolean ignoreCase = false;

    public NameScanFilter(String name) {
        this.namesFilter = new String[]{name};
    }

    public NameScanFilter(String[] names) {
        this.namesFilter = names;
    }

    public NameScanFilter(String[] names, boolean ignoreUnknown, boolean ignoreCase) {
        this.namesFilter = names;
        this.ignoreUnknown = ignoreUnknown;
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean isMatch(BluetoothDevice device, int rssi) {
        String name = device.getName();

        if (ignoreUnknown && TextUtils.isEmpty(name)) {
            return false;
        }

        // filter by name
        if (namesFilter != null && namesFilter.length > 0) {
            for (String filterName : namesFilter) {
                if (TextUtils.isEmpty(filterName) && (TextUtils.isEmpty(name))) {
                    return true;
                } else if (!TextUtils.isEmpty(filterName) && (!TextUtils.isEmpty(name))) {
                    if (ignoreCase) {
                        if (name.toUpperCase(Locale.ENGLISH).startsWith(filterName.toUpperCase(Locale.ENGLISH))) {
                            return true;
                        }
                    } else {
                        if (name.startsWith(filterName)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            // 如果没有filter，表示没有匹配限制。
            return true;
        }
    }
}
