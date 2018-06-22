package xyz.gangle.bleconnector.presentation.comparators;

import android.bluetooth.BluetoothProfile;

import com.gangle.nble.NBle;
import com.gangle.nble.NBleDevice;

import java.util.Comparator;

/**
 * Created by yiyidu on 6/1/16.
 */

public class RssiComparator implements Comparator<NBleDevice> {

    @Override
    public int compare(NBleDevice itemBean1, NBleDevice itemBean2) {
        // 处于维护状态的排在前列
        boolean isMaintain1 = NBle.manager().isMaintain(itemBean1);
        boolean isMaintain2 = NBle.manager().isMaintain(itemBean2);
        if (isMaintain1 && isMaintain2) {
            return 0;
        } else if (isMaintain1) {
            return -1;
        } else if (isMaintain2) {
            return 1;
        }

        // 连接成功的排在前列
        if (itemBean1.getConnectionState() == BluetoothProfile.STATE_CONNECTED && itemBean2.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            return 0;
        } else if (itemBean1.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            return -1;
        } else if (itemBean2.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
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
