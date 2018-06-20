package xyz.gangle.bleconnector.data;

import android.content.Context;

import com.gangle.nble.DeviceStateEvent;
import com.gangle.nble.ifunction.INBleNotifyFunction;
import com.gangle.util.CommonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.UUID;


public class BaseBleNotifyFunction implements INBleNotifyFunction {

    @Override
    public UUID[] getNotifyUuid() {
        return null;
    }

    @Override
    public void onNotify(Context context, String address, UUID uuid, byte[] value) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.NOTIFY, address, uuid, value));
    }

    @Override
    public void onRead(Context context, String address, UUID characUuid, byte[] value) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.ONREAD, address, characUuid, value));
    }

    @Override
    public void onWrite(Context context, String address, UUID characUuid, byte[] value) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.ONWRITE, address, characUuid, value));
    }

    @Override
    public void onConnectStart(Context context, String address) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.CONNECT_START, address));
    }

    @Override
    public void onConnected(Context context, String address) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.CONNECTED, address));
    }

    @Override
    public void onConnecting(Context context, String address) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.CONNECTING, address));
    }

    @Override
    public void onDisconnected(Context context, String address) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.DISCONNECTED, address));
    }

    @Override
    public void onConnectFinish(Context context, String address) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.CONNECT_FINISH, address));
    }

    @Override
    public void onRssi(Context context, String address, int rssi) {
        postEvent(new DeviceStateEvent(DeviceStateEvent.RSSI, address, CommonUtil.int2byte(rssi)));
    }

    private void postEvent(DeviceStateEvent event) {
        getEventBus().post(event);
    }

    private EventBus getEventBus() {
        return EventBus.getDefault();
    }

}
