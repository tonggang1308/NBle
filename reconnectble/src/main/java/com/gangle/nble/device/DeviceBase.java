package com.gangle.nble.device;

/**
 * Created by Gang Tong on 4/14/15.
 */
public class DeviceBase {

    private String address;
    private String name;

    public DeviceBase(String address, String name) {
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
