package com.gangle.nble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BluetoothAdapterStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("onReceive() intent.getAction():%s, flags:0x%X", intent.getAction(), intent.getFlags());

        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            LogUtils.d(intent.getAction() + ", State: " + NBleUtil.adapterStateToString(state));
            if (BluetoothAdapter.STATE_ON == state) {
                NBleService.startAdapterTurnOn(context);
            } else if (BluetoothAdapter.STATE_TURNING_OFF == state) {
                NBleService.startAdapterTurnOff(context);
            } else if (BluetoothAdapter.STATE_OFF == state) {
            }
        } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)
                || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            NBleService.start(context);
        }
    }

}
