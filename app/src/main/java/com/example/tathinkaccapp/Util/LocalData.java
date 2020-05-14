package com.example.tathinkaccapp.Util;

import com.clj.fastble.data.BleDevice;

public class LocalData {
    private static String BLE = "BLE";
    public static BleDevice mBleDevice;
    // @SEO 연결된 Ble 디바이스 정보를 static 형식으로 저장한다. -> 다른 액티비티에 리턴해주기위해.
    public static void setConnectedDevice(BleDevice device){
        mBleDevice = device;
    }

    // @SEO 저장했었던 연결된 Ble 디바이스를 초기화 한다.
    public static void removeConntectedDevice(){
        mBleDevice = null;
    }

    // @SEO 저장했었던 연결된 Ble 디바이스 정보를 리턴한다.
    public static BleDevice getConnectedDevice(){
        return mBleDevice;
    }
}
