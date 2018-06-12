package com.gangle.nble.ScanFilter;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

/**
 * Created by Gang Tong on 2018/6/12.
 */
public class AddressScanFilter implements IScanFilter {
    private static final String MAC_ADDRESS_REGEX = "^([0-9a-fA-F]{2})(([/\\s:-][0-9a-fA-F]{2}){5})$";
    private String macStartFilter;
    private String macEndFilter;

    public AddressScanFilter(String mac) {
        this(mac, mac);
    }

    /**
     * 如果start值大于end值，则表示range是跨越FF:FF:FF:FF:FF:FF和00:00:00:00:00:00
     * @param macStart
     * @param macEnd
     */
    public AddressScanFilter(String macStart, String macEnd) {
        assert macStart.matches(MAC_ADDRESS_REGEX) : "Not match AA:BB:CC:DD:EE:FF";
        assert macEnd.matches(MAC_ADDRESS_REGEX) : "Not match AA:BB:CC:DD:EE:FF";
        this.macStartFilter = macStart;
        this.macEndFilter = macEnd;
    }


    @Override
    public boolean isMatch(BluetoothDevice device, int rssi) {
        String address = device.getAddress();
        if (!TextUtils.isEmpty(address)
                && address.compareToIgnoreCase(macStartFilter) >= 0
                && address.compareToIgnoreCase(macEndFilter) <= 0) {
            return true;
        }

        return false;
    }
}
