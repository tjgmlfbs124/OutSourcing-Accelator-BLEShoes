package com.example.tathinkaccapp.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class BluetoothBridge {

    // Bluetooth Setting Enum
    public static final int REQUEST_SELECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    public static final int UART_PROFILE_CONNECTED = 20;
    public static final int UART_PROFILE_DISCONNECTED = 21;

    public enum BleCommand {NoCommand, StartSingleCapture, StartStreaming, StopStreaming, ChangeResolution, ChangePhy, GetBleParams};
    public enum AppLogFontType {APP_NORMAL, APP_ERROR, PEER_NORMAL, PEER_ERROR};
    private String mLogMessage = "";

    // Ble class
    public BleDataTransferService mService = null;

    // BlueTooth Class
    public BluetoothAdapter mBtAdapter = null;

    // Bluetooth Tx Buffer
    public boolean mMtuRequested;

    public boolean mConnectionSuccess = false;

    // Bluetooth Flags
    public boolean mStreamActive = false;
    public int mState = UART_PROFILE_DISCONNECTED;
    private static BluetoothBridge bluetoothBridge;

    private BluetoothBridge(){ }

    public static synchronized BluetoothBridge getInstance( ) {
        if (bluetoothBridge == null)
            bluetoothBridge = new BluetoothBridge();
        return bluetoothBridge;
    }

}
