package xyz.gangle.bleconnector.presentation;

import java.util.Comparator;

import com.tggg.nble.NBleDeviceManager;

/**
 * Created by yiyidu on 6/1/16.
 */

public class RssiComparator implements Comparator<DeviceInfo> {

    @Override
    public int compare(DeviceInfo itemBean1, DeviceInfo itemBean2) {
        // 处于维护状态的排在前列
        boolean isMaintain1 = NBleDeviceManager.getInstance().isMaintain(itemBean1.address);
        boolean isMaintain2 = NBleDeviceManager.getInstance().isMaintain(itemBean2.address);
        if (isMaintain1 && isMaintain2) {
            return 0;
        } else if (isMaintain1) {
            return -1;
        } else if (isMaintain2) {
            return 1;
        }

        // 连接成功的排在前列
        if (itemBean1.status == DeviceInfo.CONNECTED && itemBean2.status == DeviceInfo.CONNECTED) {
            return 0;
        } else if (itemBean1.status == DeviceInfo.CONNECTED) {
            return -1;
        } else if (itemBean2.status == DeviceInfo.CONNECTED) {
            return 1;
        }

        // rssi信号强的排在前列
        if (itemBean1.rssi == null && itemBean2.rssi == null) {
            return 0;
        } else if (itemBean1.rssi == null) {
            return 1;
        } else if (itemBean2.rssi == null) {
            return -1;
        }
        return itemBean2.rssi - itemBean1.rssi;
    }

}
