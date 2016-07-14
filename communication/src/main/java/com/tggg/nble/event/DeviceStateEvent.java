package com.tggg.nble;

import java.util.UUID;

/**
 * Created by Gang Tong on 4/15/15.
 */
public class DeviceStateEvent {
    public static final int NOTIFY = 0x0001;
    public static final int ONREAD = 0x0002;
    public static final int ONWRITE = 0x0003;
    public static final int CONNECTED = 0x0004;
    public static final int DISCONNECTED = 0x005;
    public static final int DISCOVERED = 0x006;
    public static final int CONNECTING = 0x007;
    public static final int CLOSE = 0x008;
    public static final int RSSI = 0x009;

    public final int type;
    public final String address;
    public final UUID uuid;
    public final byte[] value;

    /**
     * @param type
     * @param address address of affected BLE device
     */

    public DeviceStateEvent(int type, String address) {
        this(type, address, null);
    }

    public DeviceStateEvent(int type, String address, byte[] value) {
        this(type, address, null, value);
    }

    public DeviceStateEvent(int type, String address, UUID uuid, byte[] value) {
        this.type = type;
        this.address = address;
        this.value = value;
        this.uuid = uuid;
    }

    public String typeString() {
        switch (type) {
            case NOTIFY:
                return "NOTIFY";

            case ONREAD:
                return "ON_READ";

            case ONWRITE:
                return "ON_WRITE";

            case CONNECTED:
                return "CONNECTED";

            case DISCONNECTED:
                return "DISCONNECTED";

            case DISCOVERED:
                return "DISCOVERED";

            case CONNECTING:
                return "CONNECTING";

            case CLOSE:
                return "CLOSE";

            case RSSI:
                return "RSSI";
        }
        return "Unknow";
    }

}
