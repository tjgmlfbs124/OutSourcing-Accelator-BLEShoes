package com.example.tathinkaccapp.BLE;

import android.app.Service;
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
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import android.support.v4.content.LocalBroadcastManager;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BleDataTransferService extends Service {

    private final static String TAG = "lbs_tag_service";//ImageTransferService.class.getSimpleName();
    boolean flag = false;
    boolean auto_flag = false;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGatt mBluetoothGatt2;

    private int mConnectionState = STATE_DISCONNECTED;



    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.physiocnis.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.physiocnis.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.physiocnis.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.physiocnis.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_IMG_INFO_AVAILABLE =
            "com.physiocnis.bluetooth.le.ACTION_IMG_INFO_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.physiocnis.bluetooth.le.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER =
            "com.physiocnis.bluetooth.le.DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER";

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    public static final UUID DATA_AUDIO_TRANSFER_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca3e");
    public static final UUID RX_CHAR_UUID       = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca3e");
    public static final UUID TX_CHAR_UUID       = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca3e");
    public static final UUID IMG_INFO_CHAR_UUID = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca3e");

    public static final UUID R_RX_CHAR_UUID       = UUID.fromString("6e400012-b5a3-f393-e0a9-e50e24dcca3e");

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {        /*****  LEFT    *****/
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        String intentAction;

/*
            if(auto_flag == false){
                flag = false;
                enableTXNotification();
            }
            auto_flag = false;*/


        if (newState == BluetoothProfile.STATE_CONNECTED) {
            intentAction = ACTION_GATT_CONNECTED;
            mConnectionState = STATE_CONNECTED;
            broadcastUpdate(intentAction);
            // Attempts to discover services after successful connection.
            Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            intentAction = ACTION_GATT_DISCONNECTED;
            mConnectionState = STATE_DISCONNECTED;
            Log.i(TAG, "Disconnected from GATT server.");
            broadcastUpdate(intentAction);

        }
    }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt );

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(IMG_INFO_CHAR_UUID.equals(characteristic.getUuid())) {
                broadcastUpdate(ACTION_IMG_INFO_AVAILABLE, characteristic);
            }
            else {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            Log.w(TAG, "OnCharWrite");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.w(TAG, "OnDescWrite!!!");
            if(TX_CHAR_UUID.equals(descriptor.getCharacteristic().getUuid())) {
                // When the first notification is set we can set the second
                BluetoothGattService ImageTransferService = mBluetoothGatt.getService(DATA_AUDIO_TRANSFER_SERVICE_UUID);
                BluetoothGattCharacteristic ImgInfoChar = ImageTransferService.getCharacteristic(IMG_INFO_CHAR_UUID);
                if (ImgInfoChar == null) {
                    showMessage("Img Info characteristic not found!");
                    broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
                    return;
                }
                mBluetoothGatt.setCharacteristicNotification(ImgInfoChar, true);

                BluetoothGattDescriptor descriptor2 = ImgInfoChar.getDescriptor(CCCD);
                descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor2);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.w(TAG, "MTU changed: " + String.valueOf(mtu));
        }
    };


    private final BluetoothGattCallback mGattCallback2 = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt2.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt2 = " + mBluetoothGatt2 );

                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if(IMG_INFO_CHAR_UUID.equals(characteristic.getUuid())) {
                broadcastUpdate(ACTION_IMG_INFO_AVAILABLE, characteristic);
            }
            else {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
            Log.w(TAG, "OnCharWrite");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.w(TAG, "OnDescWrite!!!");
            if(TX_CHAR_UUID.equals(descriptor.getCharacteristic().getUuid())) {
                // When the first notification is set we can set the second
                BluetoothGattService ImageTransferService = mBluetoothGatt2.getService(DATA_AUDIO_TRANSFER_SERVICE_UUID);
                BluetoothGattCharacteristic ImgInfoChar = ImageTransferService.getCharacteristic(IMG_INFO_CHAR_UUID);
                if (ImgInfoChar == null) {
                    showMessage("Img Info characteristic not found!");
                    broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
                    return;
                }
                mBluetoothGatt2.setCharacteristicNotification(ImgInfoChar, true);

                BluetoothGattDescriptor descriptor2 = ImgInfoChar.getDescriptor(CCCD);
                descriptor2.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt2.writeDescriptor(descriptor2);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.w(TAG, "MTU changed: " + String.valueOf(mtu));
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {

            // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        } else if(IMG_INFO_CHAR_UUID.equals(characteristic.getUuid())) {
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
        } else {

        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BleDataTransferService getService() {
            return BleDataTransferService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        else{
            if(!mBluetoothAdapter.isEnabled())
                mBluetoothAdapter.enable();
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */

    public boolean connect(final String address) {

        auto_flag = true;

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        if(device.getName().equals("SHOE MONITOR R")){
            mBluetoothGatt2 = device.connectGatt(this, false, mGattCallback2);
            Log.d("TAK", "RIGHT ENTER");
            flag = true;
        }else if(device.getName().equals("SHOE MONITOR L")){
            mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
            Log.d("TAK", "LEFT ENTER");
            flag = false;
        }


        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public boolean isConnected(){
        return (mConnectionState == STATE_CONNECTED);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        mBluetoothGatt2.disconnect();
        // mBluetoothGatt.close();
    }

    public void requestMtu(int mtu){
        Log.i(TAG, "Requesting 247 byte MTU");

        mBluetoothGatt.requestMtu(mtu);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    /*
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }*/

    /**
     * Enable TXNotification
     *
     * @return
     */
    public void enableTXNotification()
    {
        Log.w(TAG, "enable TX not.");
        try{
            if(flag == false) {
                BluetoothGattService ImageTransferService = mBluetoothGatt.getService(DATA_AUDIO_TRANSFER_SERVICE_UUID);  // FLAG FALSE = LEFT
                BluetoothGattCharacteristic TxChar = ImageTransferService.getCharacteristic(TX_CHAR_UUID);
                mBluetoothGatt.setCharacteristicNotification(TxChar, true);
                BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }else {
                BluetoothGattService ImageTransferService = mBluetoothGatt2.getService(DATA_AUDIO_TRANSFER_SERVICE_UUID);  // FLAG TRUE = RIGHT
                BluetoothGattCharacteristic TxChar = ImageTransferService.getCharacteristic(TX_CHAR_UUID);
                mBluetoothGatt2.setCharacteristicNotification(TxChar, true);
                BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt2.writeDescriptor(descriptor);
            }
        }
        catch (Exception e){
            Log.i("seo","error : " + e);
        }

    }

    public void writeRXCharacteristic(byte[] value)
    {
        if(mBluetoothGatt != null) {
            BluetoothGattService RxService = mBluetoothGatt.getService(DATA_AUDIO_TRANSFER_SERVICE_UUID);
            if (RxService == null) {
                showMessage("Rx service not found!");
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
            if (RxChar == null) {
                showMessage("Rx characteristic not found!");
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
                return;
            }
            RxChar.setValue(value);
            boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        }

    }

    public void sendCommand(int command, byte []data) {
        byte []pckData;
        pckData = new byte[7];
        pckData[0] = 0x0A;
        pckData[1] = 0X0B;
        pckData[2] = (byte)command;
        pckData[3] = 0X00;
        pckData[4] = 0x00;
        pckData[5] = 0X0A;
        pckData[6] = 0X0B;
        writeRXCharacteristic(pckData);
    }



    /********************** TEST *************************/
    public void R_writeRXCharacteristic(byte[] value)
    {
        if(mBluetoothGatt2 != null) {

            BluetoothGattService RxService = mBluetoothGatt2.getService(DATA_AUDIO_TRANSFER_SERVICE_UUID);
            if (RxService == null) {
                showMessage("Rx service not found!");
                broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(R_RX_CHAR_UUID);
            if (RxChar == null) {
                showMessage("R_Rx characteristic not found!");
                broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
                return;
            }
            RxChar.setValue(value);
            boolean status = mBluetoothGatt2.writeCharacteristic(RxChar);

        }

    }

    public void R_sendCommand(int command, byte []data) {
        byte []pckData;
        pckData = new byte[7];
        pckData[0] = 0x0A;
        pckData[1] = 0X0B;
        pckData[2] = (byte)command;
        pckData[3] = 0X00;
        pckData[4] = 0x00;
        pckData[5] = 0X0A;
        pckData[6] = 0X0B;
        R_writeRXCharacteristic(pckData);
    }
    /********************** TEST *************************/

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
