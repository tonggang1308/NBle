package com.gangle.nble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import timber.log.Timber;

/**
 * Created by Gang Tong.
 */
public final class NBleUtil {

    public static String adapterStateToString(int state) {
        switch (state) {

            case BluetoothAdapter.STATE_CONNECTED:
                return "STATE_CONNECTED";
            case BluetoothAdapter.STATE_DISCONNECTED:
                return "STATE_DISCONNECTED";
            case BluetoothAdapter.STATE_CONNECTING:
                return "STATE_CONNECTING";
            case BluetoothAdapter.STATE_DISCONNECTING:
                return "STATE_DISCONNECTING";
            case BluetoothAdapter.STATE_OFF:
                return "STATE_OFF";
            case BluetoothAdapter.STATE_ON:
                return "STATE_ON";
            case BluetoothAdapter.STATE_TURNING_OFF:
                return "STATE_TURNING_OFF";
            case BluetoothAdapter.STATE_TURNING_ON:
                return "STATE_TURNING_ON";
            default:
                return "unknown state:" + state;
        }
    }


    public static String connectionStateToString(int state) {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                return "STATE_CONNECTED";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "STATE_DISCONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "STATE_CONNECTING";
            case BluetoothProfile.STATE_DISCONNECTING:
                return "STATE_DISCONNECTING";
            default:
                return "unknown state:" + state;
        }
    }

    public static String statusToString(int status) {
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                return "GATT_SUCCESS";
            case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                return "GATT_READ_NOT_PERMITTED";
            case BluetoothGatt.GATT_WRITE_NOT_PERMITTED:
                return "GATT_WRITE_NOT_PERMITTED";
            case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                return "GATT_REQUEST_NOT_SUPPORTED";
            case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                return "GATT_INSUFFICIENT_AUTHENTICATION";
            case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                return "GATT_INSUFFICIENT_ENCRYPTION";
            case BluetoothGatt.GATT_INVALID_OFFSET:
                return "GATT_INVALID_OFFSET";
            case BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH:
                return "GATT_INVALID_ATTRIBUTE_LENGTH";
            case BluetoothGatt.GATT_FAILURE:
                return "GATT_FAILURE";
            default:
                return "unknown state:" + status;
        }
    }

    public static String gattToString(BluetoothGatt gatt) {
        if (gatt == null) {
            return "null";
        }
        return "gatt:" + gatt.getDevice().getName();
    }

    /**
     * 是否支持BLE功能
     */
    public static boolean isLESupport(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 判断蓝牙是否打开
     */
    public static boolean isAdapterEnable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothAdapter == null) {
                return false;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Timber.v("bluetoothAdapter DISABLE!");
            }
            return bluetoothAdapter.isEnabled();
        } else {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            return adapter == null ? false : BluetoothAdapter.getDefaultAdapter().isEnabled();
        }
    }

    /**
     * 不提示，直接关闭蓝牙
     */
    public static void disableBluetoothWithoutNotification(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.disable();
        }
    }

    /**
     * 不提示，直接打开蓝牙
     */
    public static void enableBluetoothWithoutNotification(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * 获取打开系统设置页面的intent
     */
    public static Intent getOpenSystemBluetoothSettingActivityIntent() {
        return new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
    }
}
