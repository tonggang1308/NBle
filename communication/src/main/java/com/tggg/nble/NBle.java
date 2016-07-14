package com.tggg.nble;

import android.content.Context;

import com.tggg.nble.NBlePreference;

import java.util.UUID;

/**
 * Created by Gang Tong
 */
public final class NBle {
    private NBle() {
    }

    static public void init(Context context) {
        // BLE Share Preference
        NBlePreference.init(context);

        // BLE Service
        NBleService.start(context);
    }

    public static class ScannerBuilder {
        NBleScannerImpl nBleScanner;

        public ScannerBuilder(Context context) {
            nBleScanner = new NBleScannerImpl(context);
        }

        public ScannerBuilder setPeriod(long period) {
            nBleScanner.setPeriod(period);
            return this;
        }


        public ScannerBuilder setUuids(UUID[] uuids) {
            nBleScanner.setUuids(uuids);
            return this;
        }

        public ScannerBuilder setScanName(String scanName) {
            nBleScanner.setScanName(scanName);
            return this;
        }

        public ScannerBuilder setScanNames(String[] scanNames) {
            nBleScanner.setScanNames(scanNames);
            return this;
        }

        public NBleScanner build() {
            return nBleScanner;
        }
    }
}
