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
import android.support.annotation.NonNull;

import com.gangle.nble.Record.StatusChangeRecord;
import com.gangle.nble.device.DeviceBase;
import com.gangle.nble.ifunction.INBleNotifyFunction;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;


/**
 * Created by Gang Tong.
 */
class NBleDeviceImpl extends DeviceBase implements NBleDevice {

    private static final int OPERATION_TIME_OUT = 5;
    /**
     * Enable Notification的UUID
     */
    public static final UUID DESCRIPTOR_ENABLE_NOTIFICATION = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private Context context;
    private BluetoothGatt bleGatt;
    private BluetoothAdapter bluetoothAdapter;
    private ObservableEmitter<Object> mEmitter;

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
            BluetoothGattCharacteristic chara = getCharacteristic(bleGatt, serviceUuid, characteristicUuid);
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
    public synchronized void write(final UUID serviceUuid, final UUID characteristicUuid, final byte[] data) {
        Observable observable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                mEmitter = emitter;
                try {
                    BluetoothGattCharacteristic characteristic = getCharacteristic(bleGatt, serviceUuid, characteristicUuid);
                    // 需要写入的数据
                    characteristic.setValue(data);
                    Preconditions.checkState(bleGatt.writeCharacteristic(characteristic), "write Characteristic Fail!");

                    // 如果此characteristic是没有返回值的
                    if ((characteristic.getWriteType() & WRITE_TYPE_NO_RESPONSE) == 0) {
                        mEmitter.onComplete();
                    }

                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                    mEmitter.onError(new Throwable(e.getMessage()));
                }
            }
        }).doOnNext(new Consumer<Object>() {
            @Override
            public void accept(Object data) throws Exception {
                LogUtils.d("Write confirm: " + getAddress() + "))" + data);
                getNotifyFunction().onWrite(context, getAddress(), characteristicUuid, (byte[]) data);
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                getNotifyFunction().onWrite(context, getAddress(), characteristicUuid, null);
            }
        }).timeout(OPERATION_TIME_OUT, TimeUnit.SECONDS);

        // 添加到执行队列
        manager().operationManager.pend(observable);
    }

    /**
     * read接口，把操作丢给manager来管理
     */
    @Override
    public synchronized void read(final UUID serviceUuid, final UUID characteristicUuid) {
        Observable observable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                mEmitter = emitter;
                try {
                    BluetoothGattCharacteristic characteristic = getCharacteristic(bleGatt, serviceUuid, characteristicUuid);
                    Preconditions.checkState(bleGatt.readCharacteristic(characteristic), "read Characteristic Fail!");
                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                    mEmitter.onError(new Throwable(e.getMessage()));
                }
            }
        }).doOnNext(new Consumer<Object>() {
            @Override
            public void accept(Object data) throws Exception {
                LogUtils.d("Read from: " + getAddress() + "))" + data);
                getNotifyFunction().onRead(context, getAddress(), characteristicUuid, (byte[]) data);
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                getNotifyFunction().onRead(context, getAddress(), characteristicUuid, null);
            }
        }).timeout(OPERATION_TIME_OUT, TimeUnit.SECONDS);

        // 添加到执行队列
        manager().operationManager.pend(observable);
    }

    @NonNull
    protected BluetoothGattCharacteristic getCharacteristic(BluetoothGatt bleGatt, UUID serviceUuid, UUID characteristicUuid) {
        Preconditions.checkNotNull(bleGatt, "gatt not connected: %s", getAddress());
        BluetoothGattService service = Preconditions.checkNotNull(bleGatt.getService(serviceUuid), "service null: %s", serviceUuid.toString());
        return Preconditions.checkNotNull(service.getCharacteristic(characteristicUuid), "characteristic null: %s", characteristicUuid.toString());
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
        return connectDirectly();
    }

    /**
     * Auto connect to the device
     */
    public boolean connectAuto() {
        // 正常断开后，如果需要重连，则继续重连
        if (bleGatt.connect()) {
            LogUtils.d("When get STATE_DISCONNECTED, gatt.connectImpl() return TRUE! address:%s", getAddress());
            isConnecting = true;
            recordStatus(StatusChangeRecord.AUTOCONNECT);
            getNotifyFunction().onConnecting(context, getAddress());
        } else return false;
        return true;
    }

    /**
     * Connect to the device directly
     */
    public boolean connectDirectly() {
        return connectImpl(false);
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


    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            final String address = gatt.getDevice().getAddress();
            NBleDevice device = manager().getDevice(address);
            LogUtils.i(getName() + ", " + address + ", operation result: " + NBleUtil.statusToString(status) + ", New connection state: " + NBleUtil.connectionStateToString(newState) + ", " + getNotifyFunction().getClass().getSimpleName());

//            String deviceName = gatt.getDevice().getName();
//            // 更新device name
//            if (deviceName != null) {
//                setName(deviceName);
//            }

            try {
                isConnecting = false;
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:

                        Preconditions.checkState(status == BluetoothGatt.GATT_SUCCESS,
                                "connect fail, status is 0x%X", status);

                        recordStatus(StatusChangeRecord.CONNECTED);

                        gatt.discoverServices();

                        getNotifyFunction().onConnected(context, gatt.getDevice().getAddress());
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:

                        // 由于某些非主动删除的原因，导致的disconnect，需要重新连接。例如：距离变远、断电、等。
                        // 如果用户主动disconnect，需要手动removeFromMaintain，否则也会重新连接。
                        recordStatus(StatusChangeRecord.DISCONNECTED);

                        getNotifyFunction().onDisconnected(context, address);

                        // status == GATT_FAILURE, 属于connectGatt时,registerClient失败，需要close后重连
                        // status == 133, 属于异常断开，需要close后重连
                        Preconditions.checkState((status == BluetoothGatt.GATT_SUCCESS), "Abnormal disconnect!");

                        close();

                        // 正常断开后，通知manager
                        manager().onDisconnected(device, null);
                    default:
                        // NO OP
                }
            } catch (Exception e) {
                recordStatus(StatusChangeRecord.CONNECTED_ERROR);
                getNotifyFunction().onDisconnected(context, address);
                close();
                // 异常断开后，通知manager
                manager().onDisconnected(device, e);
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

            getNotifyFunction().onServicesDiscovered(context, gatt.getDevice().getAddress());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                Preconditions.checkState(status == BluetoothGatt.GATT_SUCCESS);
                mEmitter.onNext(characteristic.getValue());
                mEmitter.onComplete();
            } catch (Exception e) {
                mEmitter.onError(new Throwable(e.getMessage()));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            try {
                Preconditions.checkState(status == BluetoothGatt.GATT_SUCCESS);
                mEmitter.onNext(characteristic.getValue());
                mEmitter.onComplete();
            } catch (Exception e) {
                mEmitter.onError(new Throwable(e.getMessage()));
            }
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
}
