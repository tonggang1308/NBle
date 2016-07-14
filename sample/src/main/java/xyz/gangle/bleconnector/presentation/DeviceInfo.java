package xyz.gangle.bleconnector.presentation;

/**
 * Created by yiyidu on 5/30/16.
 */

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
    int status; // 0:disconnted; 1:connected; 2:connecting;
}
