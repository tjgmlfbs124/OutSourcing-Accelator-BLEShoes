package com.example.tathinkaccapp.BLE;

import android.bluetooth.BluetoothDevice;

public class DeviceManager {
    public static BluetoothDevice leftDevice = null;
    public static BluetoothDevice rightDevice = null;

    public static void addDevice(BluetoothDevice device){
        String name = device.getName();
        char dir = name.charAt(name.length() -1);
        switch (dir){
            case 'R' :
                if(rightDevice == null)
                    rightDevice = device;
                break;
            case 'L' :
                if(leftDevice == null)
                    leftDevice = device;
                break;
        }
    }

    public static void clearDevice(){
        rightDevice = null;
        leftDevice = null;
    }

    public static BluetoothDevice getDevice(String dir){
        if(dir.equals("L")) return leftDevice;
        else return rightDevice;
    }
}
