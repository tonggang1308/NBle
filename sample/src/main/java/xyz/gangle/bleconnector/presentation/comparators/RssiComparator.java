package xyz.gangle.bleconnector.presentation.comparators;

import com.gangle.nble.NBle;

import java.util.Comparator;

import xyz.gangle.bleconnector.data.DeviceInfo;

/**
 * Created by yiyidu on 6/1/16.
 */

public class RssiComparator implements Comparator<DeviceInfo> {

    @Override
    public int compare(DeviceInfo itemBean1, DeviceInfo itemBean2) {
        // 处于维护状态的排在前列
        boolean isMaintain1 = NBle.getManager().isMaintain(itemBean1.getAddress());
        boolean isMaintain2 = NBle.getManager().isMaintain(itemBean2.getAddress());
        if (isMaintain1 && isMaintain2) {
            return 0;
        } else if (isMaintain1) {
            return -1;
        } else if (isMaintain2) {
            return 1;
        }

        // 连接成功的排在前列
        if (itemBean1.getStatus() == DeviceInfo.CONNECTED && itemBean2.getStatus() == DeviceInfo.CONNECTED) {
            return 0;
        } else if (itemBean1.getStatus() == DeviceInfo.CONNECTED) {
            return -1;
        } else if (itemBean2.getStatus() == DeviceInfo.CONNECTED) {
            return 1;
        }

        // rssi信号强的排在前列
        if (itemBean1.getRssi() == null && itemBean2.getRssi() == null) {
            return 0;
        } else if (itemBean1.getRssi() == null) {
            return 1;
        } else if (itemBean2.getRssi() == null) {
            return -1;
        }
        return itemBean2.getRssi() - itemBean1.getRssi();
    }

}
