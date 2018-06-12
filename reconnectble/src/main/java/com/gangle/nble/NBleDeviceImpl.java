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
import android.content.Context;

import com.google.gson.Gson;
import com.gangle.nble.Record.StatusChangeRecord;
import com.gangle.nble.device.DeviceBase;
import com.gangle.nble.ifunction.INBleNotifyFunction;

import java.util.List;
import java.util.UUID;

import rx.functions.Action1;
import timber.log.Timber;

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
     * 记录当前device是否在连接中。
     */
    private boolean isConnecting = false;

    /**
     * 通知接口。当有Notification达到时，调用此接口。
     */
    private INBleNotifyFunction iNBleNotifyFunction;

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


    /**
     * 获取device
     */
    public BluetoothDevice getBleDevice() {
        return this.bleGatt.getDevice();
    }

    private NBleDeviceManagerImpl getManager() {
        return NBleDeviceManagerImpl.getInstance();
    }

    /**
     * write接口，把操作丢给manager来管理
     */
    @Override
    public void write(UUID serviceUuid, UUID characteristicUuid, byte[] data) {
        getManager().writeCharacteristic(getAddress(), serviceUuid, characteristicUuid, data);
    }

    public synchronized boolean writeImpl(UUID serviceUuid, UUID characteristicUuid, byte[] data) {
        boolean retValue = true;
        if (bleGatt == null) {
            Timber.e("gatt not connected: %s", getAddress());
            retValue = false;
        } else {
            BluetoothGattService service = bleGatt.getService(serviceUuid);
            if (service == null) {
                Timber.e("service null: %s", serviceUuid.toString());
                retValue = false;
            } else {

                BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
                if (characteristic == null) {
                    Timber.e("characteristic null: %s", characteristicUuid.toString());
                    retValue = false;
                } else {
                    characteristic.setValue(data);
                    retValue = bleGatt.writeCharacteristic(characteristic);
                    Timber.i("writeCharacteristic result: %b", retValue);
                }
            }
        }
        if (!retValue) {
            getManager().onWriteCharacteristic(getAddress(), characteristicUuid, null);
        }

        return retValue;
    }

    /**
     * read接口，把操作丢给manager来管理
     */
    @Override
    public void read(UUID serviceUuid, UUID characteristicUuid) {
        getManager().readCharacteristic(getAddress(), serviceUuid, characteristicUuid);
    }

    public synchronized boolean readImpl(UUID serviceUuid, UUID characteristicUuid) {
        boolean retValue = true;
        if (bleGatt == null) {
            Timber.e("gatt not connected: %s", getAddress());
            retValue = false;
        } else {
            BluetoothGattService service = null;
            try {
                service = bleGatt.getService(serviceUuid);
            } catch (Exception e) {
                Timber.e(e.getMessage());
            }
            if (service == null) {
//            Timber.e("service null: %s", serviceUuid.toString());
//            Timber.e("service count: %d", bleGatt.getServices().size());
//            for (BluetoothGattService ser : bleGatt.getServices()) {
//                Timber.e("service uuid: %s", ser.getUuid().toString());
//            }
                retValue = false;
            } else {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
                if (characteristic == null) {
                    Timber.e("characteristic null: %s", characteristicUuid.toString());
                    retValue = false;
                } else {
                    retValue = bleGatt.readCharacteristic(characteristic);
                    Timber.i("readCharacteristic result: %b", retValue);
                }
            }
        }

        if (!retValue) {
            getManager().onReadCharacteristic(getAddress(), characteristicUuid, null);
        }
        return retValue;
    }

    public void onReadImpl(String address, UUID uuid, byte[] value) {
        if (iNBleNotifyFunction != null) {
            iNBleNotifyFunction.onRead(context, address, uuid, value);
        }
    }

    public void onWriteImpl(String address, UUID uuid, byte[] value) {
        if (iNBleNotifyFunction != null) {
            iNBleNotifyFunction.onWrite(context, address, uuid, value);
        }
    }

    public INBleNotifyFunction getNotifyFunction() {
        return this.iNBleNotifyFunction;
    }

    public void setINotifyFunction(INBleNotifyFunction iNotifyFunction) {
        this.iNBleNotifyFunction = iNotifyFunction;
    }

    /**
     * 请求Rssi值。
     */
    public synchronized void requestRemoteRssi() {
        if (bleGatt == null) {
            Timber.e("gatt not connected: %s", getAddress());
            return;
        }

        bleGatt.readRemoteRssi();
    }

    /**
     * 是否已连接
     */
    public boolean isConnected() {
        return getConnectionState() == BluetoothProfile.STATE_CONNECTED;
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
        Timber.d("getConnectedState() addr:%s, state:%s", device.getAddress(), NBleUtil.connectionStateToString(state));

        if (isConnecting) {
            state = BluetoothProfile.STATE_CONNECTING;
            Timber.d("getConnectedState() addr:%s, return state:%s", device.getAddress(), NBleUtil.connectionStateToString(state));
            return state;
        }

        state = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getConnectionState(device, BluetoothProfile.GATT);
        Timber.d("getConnectedState() addr:%s, return state:%s", device.getAddress(), NBleUtil.connectionStateToString(state));

        return state;
    }

    /**
     * 断开连接。
     */
    @Override
    public synchronized void disconnect() {
        getManager().disconnect(this);
    }

    /**
     * 断开连接。
     */
    public synchronized void disconnectImpl() {
        if (bleGatt != null) {
            Timber.i("disconnect() isConnecting:%s, address: %s", Boolean.toString(isConnecting), getAddress());
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
        return getManager().connectDirectly(this);
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
            Timber.e("invalid address");
            return false;
        }

        int state = getConnectionState();
        if (state != BluetoothProfile.STATE_DISCONNECTED || isConnecting) {
            Timber.w("Current state is %s, so cannot do connectImpl operation. isConnecting: %b", state, isConnecting);
            return false;
        }

        // 如果bleGate只是disconnect了，没有close。并再尝试通过connectGatt连接时，会导致2次回调。
        if (bleGatt != null) bleGatt.close();

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(getAddress());
        Timber.i("Start connectGatt() address: %s, autoConnect:%b", getAddress(), autoConnect);
        isConnecting = true;

        recordStatus(StatusChangeRecord.CONNECT);
        if (iNBleNotifyFunction != null) {
            iNBleNotifyFunction.onConnectStart(context, getAddress());
        }

        bleGatt = bluetoothDevice.connectGatt(context, autoConnect, gattCallBack);

        if (bleGatt == null) {
            isConnecting = false;
            recordStatus(StatusChangeRecord.CONNECTED_ERROR);

            // onConnectFinish 是根据bleGatt来判定是否要调用。
            if (iNBleNotifyFunction != null) {
                iNBleNotifyFunction.onConnectFinish(context, getAddress());
            }
            return false;
        } else {
            if (iNBleNotifyFunction != null) {
                iNBleNotifyFunction.onConnecting(context, getAddress());
            }
        }

        Timber.d("connecting address");
        return true;
    }

    @Override
    public String serialize() {
        return new Gson().toJson(new SerializeBleDeviceInfo(getAddress(), getName(), NBle.getManager().isMaintain(this)));
    }

    static public NBleDeviceImpl deserialize(Context context, String json) {
        SerializeBleDeviceInfo deviceInfo = new Gson().fromJson(json, SerializeBleDeviceInfo.class);
        NBleDeviceImpl device = new NBleDeviceImpl(context, deviceInfo.address, deviceInfo.name);
        if (deviceInfo.maintain != null) {
            NBle.getManager().setMaintain(deviceInfo.address, deviceInfo.maintain);
        }
        return device;
    }

    /**
     * close
     */
    public void close() {
        isConnecting = false;
        if (bleGatt != null) {
            bleGatt.close();
            bleGatt = null;
            recordStatus(StatusChangeRecord.CLOSE);

            if (iNBleNotifyFunction != null) {
                iNBleNotifyFunction.onConnectFinish(context, getAddress());
            }
        }
    }

    Action1<String> closeAction = new Action1<String>() {
        @Override
        public void call(String address) {
            synchronized (NBleDeviceImpl.this) {
                Timber.i("Start Close Action to close device " + address);
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

            Timber.i(getName() + ", " + address + ", " + iNBleNotifyFunction.getClass().getName() + ", Connection operation status: " + NBleUtil.statusToString(status) + ", New connection state: " + NBleUtil.connectionStateToString(newState));

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

                            if (iNBleNotifyFunction != null) {
                                iNBleNotifyFunction.onConnected(context, gatt.getDevice().getAddress());
                            }
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

                        if (iNBleNotifyFunction != null) {
                            iNBleNotifyFunction.onDisconnected(context, gatt.getDevice().getAddress());
                        }

                        if (bluetoothAdapter.isEnabled() && getManager().isMaintain(address)) {
                            Timber.d("Device " + address + " is in maintain list");
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                Timber.i(address + " gatt.connectImpl()");
                                if (gatt.connect()) {
                                    Timber.d("When get STATE_DISCONNECTED, gatt.connectImpl() return TRUE! address:%s", address);
                                    isConnecting = true;
                                    recordStatus(StatusChangeRecord.AUTOCONNECT);
                                    if (iNBleNotifyFunction != null) {
                                        iNBleNotifyFunction.onConnecting(context, gatt.getDevice().getAddress());
                                    }
                                } else {
                                    Timber.w("When get STATE_DISCONNECTED, gatt.connectImpl() return FALSE! address:%s", address);
                                    recordStatus(StatusChangeRecord.AUTOCONNECT_FAIL);
                                    throw new ConnectException();
                                }
                            } else {
                                // status == GATT_FAILURE, 属于connectGatt时,registerClient失败，需要close后重连
                                // status == 133, 属于异常断开，需要close后重连
                                throw new ConnectException();
                            }
                        } else {
                            Timber.d("bluetooth adapter is DISABLE or NOT in maintain list.");
                            throw new ConnectException();
                        }
                        break;
                    default:
                        // NO OP
                }
            } catch (ConnectException e) {

                getManager().onConnectException(NBleDeviceImpl.this, status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Timber.d("Discovered: addr:%s, name:%s", gatt.getDevice().getAddress(), gatt.getDevice().getName());
            List<BluetoothGattService> services = gatt.getServices();

            for (BluetoothGattService service : services) {
                for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {

                    if ((chara.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        gatt.setCharacteristicNotification(chara, true);
                        BluetoothGattDescriptor descriptor = chara.getDescriptor(DESCRIPTOR_ENABLE_NOTIFICATION);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean writeSuccess = gatt.writeDescriptor(descriptor);
                        Timber.i("writeSuccess %s", writeSuccess);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (gatt == null || characteristic == null || gatt.getDevice() == null) {
                return;
            }
            Timber.i("read: " + gatt.getDevice().getAddress() + "))" + characteristic.getStringValue(0) + " Status: " + status);

            getManager().onReadCharacteristic(gatt.getDevice().getAddress(), characteristic.getUuid(), status == BluetoothGatt.GATT_SUCCESS ? characteristic.getValue() : null);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Timber.d("Write confirm: " + gatt.getDevice().getAddress() + "))" + characteristic.getStringValue(0) + " status: " + status);
            getManager().onWriteCharacteristic(gatt.getDevice().getAddress(), characteristic.getUuid(), status == BluetoothGatt.GATT_SUCCESS ? characteristic.getValue() : null);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue();

            //
            // Timber.i("Notification Addr:%s\tChara:%s\tValue:%s\t", gatt.getDevice().getAddress(), characteristic.getUuid().toString(), StringUtil.getHexString(value));
            if (iNBleNotifyFunction != null) {
                UUID[] notifyUuids = iNBleNotifyFunction.getNotifyUuid();
                if (notifyUuids != null) {
                    for (UUID uuid : notifyUuids) {
                        if (uuid.equals(characteristic.getUuid())) {
                            iNBleNotifyFunction.onNotify(context, gatt.getDevice().getAddress(), characteristic.getUuid(), characteristic.getValue());
                            return;
                        }
                    }
                }
            }
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Timber.d("onDescriptorRead: " + gatt.getDevice().getAddress() + "))" + descriptor.toString() + " status: " + status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Timber.d("onDescriptorWrite: " + gatt.getDevice().getAddress() + "))" + descriptor.toString() + " status: " + status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Timber.d("onReliableWriteCompleted: " + gatt.getDevice().getAddress() + " status: " + status);

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            Timber.d("onReadRemoteRssi: " + gatt.getDevice().getAddress() + " rssi: " + rssi + " status: " + status);
            if (iNBleNotifyFunction != null) {
                iNBleNotifyFunction.onRssi(context, gatt.getDevice().getAddress(), rssi);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Timber.d(gatt.getDevice().getAddress() + " mtu: " + mtu + " status: " + status);
        }
    };

    private class SerializeBleDeviceInfo {
        /**
         * 序列化字段
         */
        public String address;
        public String name;
        public Boolean maintain;

        public SerializeBleDeviceInfo(String address, String name, boolean maintain) {
            this.address = address;
            this.name = name;
            this.maintain = maintain;
        }
    }

    private class ConnectException extends Exception {
    }
}
