package xyz.gangle.bleconnector.presentation;


import android.graphics.Color;

public class DeviceInfo {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int CONNECTING = 2;
    public static final int CLOSE = 3;

    public DeviceInfo(String address, String name, Integer rssi, int status) {
        this.address = address;
        this.name = name;
        this.rssi = rssi;
        this.status = status;
    }

    String address;
    String name;
    Integer rssi;
    int status;

    public String getRssiString() {
        return rssi == null ? "---" : String.format("%ddb", rssi);
    }

    public String getStatusString() {
        if (status == DeviceInfo.DISCONNECTED) {
            return "Disconnected";
        } else if (status == DeviceInfo.CONNECTING) {
            return "Connecting";
        } else if (status == DeviceInfo.CONNECTED) {
            return "Connected";
        } else if (status == DeviceInfo.CLOSE) {
            return "Close";
        }

        return "Unknow";
    }
}
