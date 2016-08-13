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
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

@TargetApi(21)
class NBleScannerImpl implements NBleScanner {

    private static final String MAC_ADDRESS_REGEX = "^([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})$";
    public static final int SCAN_NAME_MATCH_WHOLE = 0x00;
    public static final int SCAN_NAME_MATCH_HEAD = 0x01;
    public static final int SCAN_NAME_MATCH_TAIL = 0x02;
    public static final int SCAN_NAME_MATCH_CONTAIN = 0x03;

    private Context context;
    private UUID[] uuids;
    private String[] namesFilter;
    private String macStartFilter;
    private String macEndFilter;
    private Integer rssiFilter;
    private boolean ignoreCase = false;

    private int scanNameMatchType = SCAN_NAME_MATCH_HEAD;

    private final BluetoothAdapter.LeScanCallback mLeScanCallback; // of android
    private ScanCallback m21Scancalback;
    private BluetoothAdapter mAdapter;
    private BleScanListener mScanListener; // of this scanner

    private Handler mHandler;
    private boolean mScanning = false;
    private List<String> addressList = new ArrayList<>();

    public NBleScannerImpl(final Context context) {
        this.context = context;
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                processScanResult(device, rssi, scanRecord);
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            m21Scancalback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                        processScanResult(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
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

    protected void processScanResult(BluetoothDevice device, final int rssi, final byte[] scanRecord) {
        if (mScanListener == null) {
            Timber.e("Callback not set!");
            return;
        }

        // whether device is exist
        if (!addressList.contains(device.getAddress())) {
            Timber.v("ADDRESS:%s, RSSI:%d, NAME:%s", device.getAddress(), rssi, device.getName());
            addressList.add(device.getAddress());
        }

        // whether device match the filters
        if (isMatchMacRange(device.getAddress()) && isMatchName(device.getName()) && isMatchRssi(rssi)) {
            Timber.v("MATCH FILTER ADDRESS:%s, RSSI:%d, NAME:%s", device.getAddress(), rssi, device.getName());
            mScanListener.onDeviceDiscovered(device.getAddress(), device.getName(), rssi, scanRecord);
        }
    }

    public UUID[] getUuids() {
        return uuids;
    }

    public void setUuids(UUID[] uuids) {
        this.uuids = uuids;
    }

    public void setScanName(String scanName) {
        this.namesFilter = new String[]{scanName};
    }

    public void setScanNames(String[] scanNames) {
        this.namesFilter = scanNames;
    }

    public void setRssiLimit(Integer rssi) {
        this.rssiFilter = rssi;
    }

    public void setNameCaseIgnore(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean setMacRange(String start, String end) {
        if (start == null || end == null) {
            this.macStartFilter = start;
            this.macEndFilter = end;
            return true;
        } else if (start.matches(MAC_ADDRESS_REGEX) && end.matches(MAC_ADDRESS_REGEX)) {
            this.macStartFilter = start;
            this.macEndFilter = end;
            return true;
        }
        return false;
    }

    public void setMac(String mac) {
        setMacRange(mac, mac);
    }

    public boolean start(BleScanListener callback) {
        return start(callback, INDEFINITE);
    }

    @Override
    public boolean start(BleScanListener callback, int duration) {
        addressList.clear();
        return start(uuids, callback, duration);
    }

    private synchronized boolean start(UUID[] serviceUuids, BleScanListener listener, long duration) {
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
            if (namesFilter != null)
                Timber.d("Scan name : " + JSONArray.toJSON(namesFilter));

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
            // Stops scanning after a defined scan duration.
            if (duration > 0) {
                mHandler.postDelayed(stopRunnable, duration);
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


    /**
     * 判断是否符合name的filter
     */
    protected boolean isMatchName(String name) {
        try {
            // filter by name
            if (namesFilter != null) {
                for (String filterName : namesFilter) {
                    if (TextUtils.isEmpty(filterName) || (!TextUtils.isEmpty(name))) {
                        if (ignoreCase) {
                            if (name.toUpperCase(Locale.ENGLISH).startsWith(filterName.toUpperCase(Locale.ENGLISH))) {
                                throw new Exception("name is match");
                            }
                        } else {
                            if (name.startsWith(filterName)) {
                                throw new Exception("name is match");
                            }
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {

        }
        return true;
    }


    /**
     * 判断是否符合mac address filter
     */
    protected boolean isMatchMacRange(String address) {
        if (TextUtils.isEmpty(macStartFilter) || TextUtils.isEmpty(macStartFilter)) {
            return true;
        }

        if (!TextUtils.isEmpty(address) && address.compareToIgnoreCase(macStartFilter) >= 0 && address.compareToIgnoreCase(macEndFilter) <= 0) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否符合rssi filter
     */
    protected boolean isMatchRssi(int rssi) {
        return rssiFilter == null || rssiFilter <= rssi;
    }

    public synchronized boolean isScanning() {
        return mScanning;
    }

}
