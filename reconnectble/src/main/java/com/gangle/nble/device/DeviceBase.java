package com.gangle.nble.device;

import com.gangle.nble.Record.StatusChangeRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Gang Tong on 4/14/15.
 */
abstract public class DeviceBase {

    private String address;
    private String name;

    /**
     * 调试用。主要是记录device的各个连接事件。便于回溯历史。
     */
    private List<StatusChangeRecord> statusRecordList = Collections.synchronizedList(new ArrayList<StatusChangeRecord>());

    /**
     * 获取状态记录
     */
    public List<StatusChangeRecord> getStatusRecordList() {
        return new ArrayList<>(statusRecordList);
    }

    /**
     * 记录状态
     */
    protected void recordStatus(int status) {
        statusRecordList.add(new StatusChangeRecord(status));
    }


    public DeviceBase(String address, String name) {
        this.address = address;
        this.name = name;
    }

    abstract public String serialize();

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

    abstract public boolean connect();

    abstract public void disconnect();
}
