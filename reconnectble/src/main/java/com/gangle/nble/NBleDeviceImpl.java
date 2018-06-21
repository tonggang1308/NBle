package com.gangle.nble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;

import com.gangle.nble.Record.StatusChangeRecord;
import com.gangle.nble.device.DeviceBase;
import com.gangle.nble.ifunction.INBleNotifyFunction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import rx.functions.Action1;

/**
 * Created by Gang Tong.
 */
class NBleDeviceImpl extends DeviceBase implements NBleDevice {

    public static UUID SERVICES_DEVICE_INFO_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID CHARACTERISTICS_SOFTWARE_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");


    /**
     * Enable Notification的UUID
     */
    public static final UUID DESCRIPTOR_ENABLE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private Context context;
    private BluetoothGatt bleGatt;
    private BluetoothAdapter bluetoothAdapter;

    /**
     * rssi 信号值
     */
    private Integer rssi;

    /**
     * 记录当前device是否在连接中。
     */
    private boolean isConnecting = false;

    private NBleDeviceImpl() {
        super(null, null);
        // prevent instantiation
    }

    public NBleDeviceImpl(Context context, String address, String name) {
        super(address, name);
        this.context = context;
    }

    @Override
    public String getAddress() {
        return super.getAddress();
    }

    /**
     * 获取设备名称
     */
    public String getName() {
        return super.getName();
    }


    public Integer getRssi() {
        return this.rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


    /**
     * 获取device
     */
    public BluetoothDevice getBleDevice() {
        return this.bleGatt.getDevice();
    }

    private NBleDeviceManagerImpl manager() {
        return NBleDeviceManagerImpl.getInstance();
    }

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

    /**
     * 设置notification的状态
     */
    public void subscribe(UUID serviceUuid, UUID characteristicUuid, boolean enable) {
        try {
            Preconditions.checkNotNull(bleGatt, "gatt not connected: %s", getAddress());
            BluetoothGattService service = Preconditions.checkNotNull(bleGatt.getService(serviceUuid), "service null: %s", serviceUuid.toString());
            BluetoothGattCharacteristic chara = Preconditions.checkNotNull(service.getCharacteristic(characteristicUuid), "characteristic null: %s", characteristicUuid.toString());
            bleGatt.setCharacteristicNotification(chara, enable);
            BluetoothGattDescriptor descriptor = chara.getDescriptor(DESCRIPTOR_ENABLE_NOTIFICATION);
            descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            Preconditions.checkState(bleGatt.writeDescriptor(descriptor), "subscribe %s %s %s", enable ? "ON" : "OFF", characteristicUuid, "fail");

            LogUtils.i("subscribe %s %s %s", enable ? "ON" : "OFF", characteristicUuid, "success");
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }
    }

    /**
     * write接口，把操作丢给manager来管理
     */
    @Override
    public void write(UUID serviceUuid, UUID characteristicUuid, byte[] data) {
        manager().writeCharacteristic(getAddress(), serviceUuid, characteristicUuid, data);
    }

    public synchronized boolean writeImpl(UUID serviceUuid, UUID characteristicUuid, byte[] data) {
        try {
            Preconditions.checkNotNull(bleGatt, "gatt not connected: %s", getAddress());
            BluetoothGattService service = Preconditions.checkNotNull(bleGatt.getService(serviceUuid), "service null: %s", serviceUuid.toString());
            BluetoothGattCharacteristic characteristic = Preconditions.checkNotNull(service.getCharacteristic(characteristicUuid), "characteristic null: %s", characteristicUuid.toString());
            characteristic.setValue(data);
            Preconditions.checkState(bleGatt.writeCharacteristic(characteristic), "write Characteristic Fail!");

            manager().onWriteCharacteristic(getAddress(), characteristicUuid, null);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * read接口，把操作丢给manager来管理
     */
    @Override
    public void read(UUID serviceUuid, UUID characteristicUuid) {
        manager().readCharacteristic(getAddress(), serviceUuid, characteristicUuid);
    }

    public synchronized boolean readImpl(UUID serviceUuid, UUID characteristicUuid) {
        try {
            Preconditions.checkNotNull(bleGatt, "gatt not connected: %s", getAddress());
            BluetoothGattService service = Preconditions.checkNotNull(bleGatt.getService(serviceUuid), "service null: %s", serviceUuid.toString());
            BluetoothGattCharacteristic characteristic = Preconditions.checkNotNull(service.getCharacteristic(characteristicUuid), "characteristic null: %s", characteristicUuid.toString());
            Preconditions.checkState(bleGatt.readCharacteristic(characteristic), "read Characteristic Fail!");

            manager().onReadCharacteristic(getAddress(), characteristicUuid, null);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            return false;
        }
        return true;
    }

    public void onServicesDiscovered(String address) {
        getNotifyFunction().onServicesDiscovered(context, address);
    }

    public void onReadImpl(String address, UUID uuid, byte[] value) {
        getNotifyFunction().onRead(context, address, uuid, value);
    }

    public void onWriteImpl(String address, UUID uuid, byte[] value) {
        getNotifyFunction().onWrite(context, address, uuid, value);
    }

    public INBleNotifyFunction getNotifyFunction() {
        return NBle.manager().getNotification(getName());
    }

    /**
     * 请求Rssi值。
     */
    public synchronized void requestRemoteRssi() {
        try {
            Preconditions.checkNotNull(bleGatt, "gatt not connected: %s", getAddress());
            bleGatt.readRemoteRssi();
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }
    }


    /**
     * 获取当前设备的连接状态
     */
    public synchronized int getConnectionState() {

        if (bleGatt == null || bleGatt.getDevice() == null) {
            return BluetoothProfile.STATE_DISCONNECTED;
        }

        BluetoothDevice device = bleGatt.getDevice();
        int state = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getConnectionState(device, BluetoothProfile.GATT);
        LogUtils.d("getConnectedState() addr:%s, state:%s", device.getAddress(), NBleUtil.connectionStateToString(state));

        if (isConnecting) {
            state = BluetoothProfile.STATE_CONNECTING;
            LogUtils.d("getConnectedState() addr:%s, return state:%s", device.getAddress(), NBleUtil.connectionStateToString(state));
            return state;
        }

        state = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getConnectionState(device, BluetoothProfile.GATT);
        LogUtils.d("getConnectedState() addr:%s, return state:%s", device.getAddress(), NBleUtil.connectionStateToString(state));

        return state;
    }

    /**
     * 断开连接。
     */
    @Override
    public synchronized void disconnect() {
        manager().disconnect(this);
    }

    /**
     * 断开连接。
     */
    public synchronized void disconnectImpl() {
        if (bleGatt != null) {
            LogUtils.i("disconnect() isConnecting:%s, address: %s", Boolean.toString(isConnecting), getAddress());
            recordStatus(StatusChangeRecord.DISCONNECT);
            if (isConnecting) {
                isConnecting = false;
                bleGatt.disconnect();
            } else {
                // 如果当前在连接状态，则会触发断开的回调函数。在回调中处理是否close，以及是否需要重连。
                isConnecting = false;
                bleGatt.disconnect();
            }
        } else {
            isConnecting = false;
        }
    }

    /**
     * Connect to the device directly
     */
    public boolean connect() {
        // 当直接连接时候，一般都由于经过scan后找到的。所以，autoConnection设为false
        return manager().connectDirectly(this);
    }

    /**
     * 连接Device
     *
     * @param autoConnect， 表示是否是直连，还是auto连接。
     * @return
     */
    public synchronized boolean connectImpl(boolean autoConnect) {

        if (bluetoothAdapter == null) {
            bluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        }

        if (!bluetoothAdapter.isEnabled()) {
            return false;
        }

        if (!BluetoothAdapter.checkBluetoothAddress(getAddress())) {
            LogUtils.e("invalid address");
            return false;
        }

        int state = getConnectionState();
        if (state != BluetoothProfile.STATE_DISCONNECTED || isConnecting) {
            LogUtils.w("Current state is %s, so cannot do connectImpl operation. isConnecting: %b", state, isConnecting);
            return false;
        }

        // 如果bleGate只是disconnect了，没有close。并再尝试通过connectGatt连接时，会导致2次回调。
        if (bleGatt != null) bleGatt.close();

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(getAddress());
        LogUtils.i("Start connectGatt() address: %s, autoConnect:%b", getAddress(), autoConnect);
        isConnecting = true;

        recordStatus(StatusChangeRecord.CONNECT);
        getNotifyFunction().onConnectStart(context, getAddress());

        bleGatt = bluetoothDevice.connectGatt(context, autoConnect, gattCallBack);

        if (bleGatt == null) {
            isConnecting = false;
            recordStatus(StatusChangeRecord.CONNECTED_ERROR);

            // onConnectFinish 是根据bleGatt来判定是否要调用。
            getNotifyFunction().onConnectFinish(context, getAddress());
            return false;
        } else {
            getNotifyFunction().onConnecting(context, getAddress());
        }

        LogUtils.d("connecting address");
        return true;
    }


    /**
     * close
     */
    public void close() {
        isConnecting = false;
        if (bleGatt != null) {
            bleGatt.close();
            bleGatt = null;
            bluetoothAdapter = null;
            recordStatus(StatusChangeRecord.CLOSE);

            getNotifyFunction().onConnectFinish(context, getAddress());
        }
    }

    Action1<String> closeAction = new Action1<String>() {
        @Override
        public void call(String address) {
            synchronized (NBleDeviceImpl.this) {
                LogUtils.i("Start Close Action to close device " + address);
                // close device
                close();
            }
        }
    };

    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            final String address = gatt.getDevice().getAddress();
            String deviceName = gatt.getDevice().getName();
            NBleDevice device = manager().getDevice(address);

            LogUtils.i(getName() + ", " + address + ", Connection status: " + NBleUtil.statusToString(status) + ", New connection state: " + NBleUtil.connectionStateToString(newState) + ", " + getNotifyFunction().getClass().getSimpleName());

            try {
                isConnecting = false;
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            // 更新device name
                            if (deviceName != null) {
                                setName(deviceName);
                            }

                            recordStatus(StatusChangeRecord.CONNECTED);

                            gatt.discoverServices();

                            getNotifyFunction().onConnected(context, gatt.getDevice().getAddress());
                        } else {
                            // status == GATT_ERROR
                            // 异常断开，需要close后重连
                            recordStatus(StatusChangeRecord.CONNECTED_ERROR);
                            throw new ConnectException();
                        }
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:

                        // 由于某些非主动删除的原因，导致的disconnect，需要重新连接。例如：距离变远、断电、等。
                        // 如果用户主动disconnect，需要手动removeFromMaintain，否则也会重新连接。
                        recordStatus(StatusChangeRecord.DISCONNECTED);

                        getNotifyFunction().onDisconnected(context, gatt.getDevice().getAddress());

                        if (bluetoothAdapter.isEnabled() && manager().isMaintain(device)) {
                            LogUtils.d("Device " + address + " is in maintain list");
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                LogUtils.i(address + " gatt.connectImpl()");
                                if (gatt.connect()) {
                                    LogUtils.d("When get STATE_DISCONNECTED, gatt.connectImpl() return TRUE! address:%s", address);
                                    isConnecting = true;
                                    recordStatus(StatusChangeRecord.AUTOCONNECT);
                                    getNotifyFunction().onConnecting(context, gatt.getDevice().getAddress());
                                } else {
                                    LogUtils.w("When get STATE_DISCONNECTED, gatt.connectImpl() return FALSE! address:%s", address);
                                    recordStatus(StatusChangeRecord.AUTOCONNECT_FAIL);
                                    throw new ConnectException();
                                }
                            } else {
                                // status == GATT_FAILURE, 属于connectGatt时,registerClient失败，需要close后重连
                                // status == 133, 属于异常断开，需要close后重连
                                throw new ConnectException();
                            }
                        } else {
                            LogUtils.d("bluetooth adapter is DISABLE or NOT in maintain list.");
                            throw new ConnectException();
                        }
                        break;
                    default:
                        // NO OP
                }
            } catch (ConnectException e) {

                manager().onConnectException(NBleDeviceImpl.this, status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtils.d("Discovered: addr:%s, name:%s", gatt.getDevice().getAddress(), gatt.getDevice().getName());
            List<BluetoothGattService> services = gatt.getServices();

            for (BluetoothGattService service : services) {
                for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                    LogUtils.v("service uuid: %s, characteristic uuid: %s, properties:0x%X", service.getUuid(), chara.getUuid(), chara.getProperties());
                }
            }

            manager().onServicesDiscovered(gatt.getDevice().getAddress());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (gatt == null || characteristic == null || gatt.getDevice() == null) {
                return;
            }
            LogUtils.i("read: " + gatt.getDevice().getAddress() + "))" + characteristic.getStringValue(0) + " Status: " + status);

            manager().onReadCharacteristic(gatt.getDevice().getAddress(), characteristic.getUuid(), status == BluetoothGatt.GATT_SUCCESS ? characteristic.getValue() : null);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtils.d("Write confirm: " + gatt.getDevice().getAddress() + "))" + characteristic.getStringValue(0) + " status: " + status);
            manager().onWriteCharacteristic(gatt.getDevice().getAddress(), characteristic.getUuid(), status == BluetoothGatt.GATT_SUCCESS ? characteristic.getValue() : null);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();

            //
            // LogUtils.i("Notification Addr:%s\tChara:%s\tValue:%s\t", gatt.getDevice().getAddress(), characteristic.getUuid().toString(), StringUtil.getHexString(value));
            UUID[] notifyUuids = getNotifyFunction().getNotifyUuid();
            if (notifyUuids != null) {
                for (UUID uuid : notifyUuids) {
                    if (uuid.equals(characteristic.getUuid())) {
                        getNotifyFunction().onNotify(context, gatt.getDevice().getAddress(), characteristic.getUuid(), characteristic.getValue());
                        return;
                    }
                }
            }
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtils.d("onDescriptorRead: " + gatt.getDevice().getAddress() + "))" + descriptor.toString() + " status: " + status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            LogUtils.d("onDescriptorWrite: " + gatt.getDevice().getAddress() + "))" + descriptor.toString() + " status: " + status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            LogUtils.d("onReliableWriteCompleted: " + gatt.getDevice().getAddress() + " status: " + status);

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            LogUtils.d("onReadRemoteRssi: " + gatt.getDevice().getAddress() + " rssi: " + rssi + " status: " + status);
            getNotifyFunction().onRssi(context, gatt.getDevice().getAddress(), rssi);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            LogUtils.d(gatt.getDevice().getAddress() + " mtu: " + mtu + " status: " + status);
        }
    };

    private class ConnectException extends Exception {
    }


}
