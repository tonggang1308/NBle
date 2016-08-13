package xyz.gangle.bleconnector.data;


public class DeviceInfo {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int CONNECTING = 2;
    public static final int CLOSE = 3;

    private String address;
    private String name;
    private Integer rssi;
    private int status;


    public DeviceInfo(String address, String name, Integer rssi, int status) {
        this.address = address;
        this.name = name;
        this.rssi = rssi;
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRssi() {
        return rssi;
    }

    public int getStatus() {
        return status;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public void setStatus(int status) {
        this.status = status;
    }

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
