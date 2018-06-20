package com.gangle.nble;

import java.util.UUID;

/**
 * Created by Gang Tong on 16/7/25.
 */
class Operation {
    public static final int OP_READ_CHARACTERISTIC = 0x01;
    public static final int OP_WRITE_CHARACTERISTIC = 0x02;
    private int opType;
    private String address;
    private UUID serviceUuid;
    private UUID characteristicUuid;
    private byte[] data;

    public Operation(int type, String address, UUID serviceUuid, UUID characteristicUuid) {
        this.opType = type;
        this.address = address;
        this.serviceUuid = serviceUuid;
        this.characteristicUuid = characteristicUuid;
    }

    public Operation(int type, String address, UUID serviceUuid, UUID characteristicUuid, byte[] data) {
        this(type, address, serviceUuid, characteristicUuid);
        setData(data);
    }

    public int getType() {
        return opType;
    }

    public String getAddress() {
        return address;
    }

    public UUID getServiceUuid() {
        return serviceUuid;
    }

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
