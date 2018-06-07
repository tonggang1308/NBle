package com.gangle.nble.Record;

/**
 * 连接状态的记录。用于调试时候使用。
 */
public final class StatusChangeRecord {
    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int DISCONNECT = 2;
    public static final int CLOSE = 3;
    public static final int CONNECT = 4;
    public static final int RECONNECT = 5;
    public static final int AUTOCONNECT = 6;
    public static final int AUTOCONNECT_FAIL = 7;
    public static final int CONNECTED_ERROR = 8;
    private int type;
    private long timestamp;

    public StatusChangeRecord(int type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public int getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String toString() {
        return toString(type);
    }

    static public String toString(int type) {
        switch (type) {
            case StatusChangeRecord.DISCONNECTED:
                return "DISCONNECTED";

            case StatusChangeRecord.CONNECTED:
                return "CONNECTED";

            case StatusChangeRecord.DISCONNECT:
                return "DISCONNECT";

            case StatusChangeRecord.CLOSE:
                return "CLOSE";

            case StatusChangeRecord.CONNECT:
                return "CONNECT";

            case StatusChangeRecord.RECONNECT:
                return "RECONNECT";

            case StatusChangeRecord.AUTOCONNECT:
                return "AUTO_CONNECT";

            case StatusChangeRecord.AUTOCONNECT_FAIL:
                return "AUTO_CONNECT_FAIL";

            case StatusChangeRecord.CONNECTED_ERROR:
                return "CONNECTED_ERROR";
        }
        return "Unknow";
    }
}