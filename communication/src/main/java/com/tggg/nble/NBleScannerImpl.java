package com.tggg.nble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

@TargetApi(21)
class NBleScannerImpl implements NBleScanner {

    public static final int SCAN_NAME_MATCH_WHOLE = 0x00;
    public static final int SCAN_NAME_MATCH_HEAD = 0x01;
    public static final int SCAN_NAME_MATCH_TAIL = 0x02;
    public static final int SCAN_NAME_MATCH_CONTAIN = 0x03;

    private Context context;
    private String[] scanNames = new String[]{""};
    private long period = 0;
    private UUID[] uuids;
    private int scanNameMatchType = SCAN_NAME_MATCH_HEAD;

    private final BluetoothAdapter.LeScanCallback mLeScanCallback; // of android
    private ScanCallback m21Scancalback;
    private BluetoothAdapter mAdapter;
    private BleScanListener mScanListener; // of this scanner

    private Handler mHandler;
    private boolean mScanning = false;
    private String flowId;
    private List<String> addressList = new ArrayList<>();

    public NBleScannerImpl(final Context context) {
        this.context = context;
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                if (mScanListener == null) {
                    Timber.e("Callback not set!");
                    return;
                }
                if (!addressList.contains(device.getAddress())) {
                    Timber.v("ADDRESS:%s, RSSI:%d, NAME:%s", device.getAddress(), rssi, device.getName());
                    addressList.add(device.getAddress());
                }

                for (String scanName : scanNames) {
                    if (scanName == null || scanName.isEmpty() || (scanName != null && device.getName() != null && device.getName().startsWith(scanName))) {
                        mScanListener.onDeviceDiscovered(device.getAddress(), device.getName(), rssi, scanRecord);
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            m21Scancalback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    String name = result.getDevice().getName();
                    String address = result.getDevice().getAddress();
                    int rssi = result.getRssi();

                    Timber.v("onScanResult, name:%s, address:%s", name, address);
                    if (mScanListener == null || callbackType != ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                        Timber.e("Callback not set!");
                        return;
                    }


                    if (!addressList.contains(result.getDevice().getAddress())) {
                        Timber.d("ADDRESS:%s, RSSI:%d, NAME:%s", result.getDevice().getAddress(), result.getRssi(), result.getDevice().getName());
                        addressList.add(result.getDevice().getAddress());
                    }

                    for (String scanName : scanNames) {
                        if (scanName == null || scanName.isEmpty() || (scanName != null && name != null && name.startsWith(scanName))) {
                            mScanListener.onDeviceDiscovered(address, name, rssi, result.getScanRecord().getBytes());
                        }
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                    Timber.d("onBatchScanResults, size:%d", results.size());

                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    stop();
                    Timber.d("onScanFailed, errorCode:%d", errorCode);
                }
            };
        }

        mHandler = new Handler(context.getMainLooper());
    }

    public boolean isBluetoothEnabled() {
        return mAdapter.isEnabled();
    }

    public long getPeriod() {
        return period;
    }

    public NBleScannerImpl setPeriod(long period) {
        this.period = period;
        return this;
    }

    public UUID[] getUuids() {
        return uuids;
    }

    public NBleScannerImpl setUuids(UUID[] uuids) {
        this.uuids = uuids;
        return this;
    }

    public NBleScannerImpl setScanName(String scanName) {
        this.scanNames = new String[]{scanName};
        return this;
    }

    public NBleScannerImpl setScanNames(String[] scanNames) {
        this.scanNames = scanNames;
        return this;
    }

    public boolean start(BleScanListener callback) {
        addressList.clear();
        return start(uuids, callback, period);
    }

    private synchronized boolean start(UUID[] serviceUuids, BleScanListener listener, long period) {
        if (listener == null) {
            Timber.e("Null call back, refuse to scan!");
            return false;
        }

        if (mAdapter == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mAdapter = bluetoothManager.getAdapter();

            // 目前有手机是api 18以上，但getAdapter()有时返回的是null。
            if (mAdapter == null) {
                Timber.e("Blue Adapter is Null!");
                return false;
            }
        }


        if (!mScanning) {
            Timber.d("Scan name : " + JSONArray.toJSON(scanNames));

            mScanListener = listener;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (!mAdapter.startLeScan(serviceUuids, mLeScanCallback)) {
                    Timber.e("ble_scan_fail, startLeScan return false!");
                    return false;
                }
            } else {
                BluetoothLeScanner scanner = mAdapter.getBluetoothLeScanner();
                if (scanner != null) {
                    scanner.startScan(m21Scancalback);
                } else {
                    Timber.e("ble_scan_fail, getBluetoothLeScanner return null!");
                    return false;
                }
            }

            mScanning = true;
            mScanListener.onScanStarted();
            flowId = UUID.randomUUID().toString();
            // Stops scanning after a defined scan period.
            if (period > 0) {
                mHandler.postDelayed(stopRunnable, period);
            }
            return true;
        }

        return false;
    }

    Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    public synchronized void stop() {
        //noinspection deprecation
        mHandler.removeCallbacks(stopRunnable);
        if (mScanning) {
            mScanning = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mAdapter.stopLeScan(mLeScanCallback);
            } else {
                BluetoothLeScanner scanner = mAdapter.getBluetoothLeScanner();
                if (scanner != null) {
                    scanner.stopScan(m21Scancalback);
                }
            }


            // if the callback is an activity, it may be finished before this called
            if (mScanListener != null) {
                mScanListener.onScanStopped();
            }
        }
        mScanListener = null;
    }

    public synchronized boolean isScanning() {
        return mScanning;
    }

}
