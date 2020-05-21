package com.example.tathinkaccapp.Dialog;

import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.tathinkaccapp.BLE.DeviceManager;
import com.example.tathinkaccapp.MenuActivity;
import com.example.tathinkaccapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BluetoothDialog extends BottomSheetDialogFragment{
    private Context mContext;
    private DeviceManager mDeviceManager = new DeviceManager();
    TextView leftButton;
    TextView rightButton;
    private static final long SCAN_PERIOD = 10000; //10 seconds
    private int MY_PERMISSIONS_REQUEST_LOCATION = 2000;
    private Handler mHandler;
    private boolean mScanning;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_bt, container, false);
        mContext = view.getContext();

        leftButton = (TextView)view.findViewById(R.id.btn_LeftBle);
        rightButton = (TextView)view.findViewById(R.id.btn_RightBle);
        leftButton.setOnClickListener(mButtonClickListener);
        rightButton.setOnClickListener(mButtonClickListener);

        mHandler = new Handler();
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, "Bluetooth Low Energy not supported", Toast.LENGTH_SHORT).show();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        MenuActivity.mBluetoothAdapter = bluetoothManager.getAdapter();
        MenuActivity.mBluetoothLeScanner = (MenuActivity.mBluetoothAdapter).getBluetoothLeScanner();
        // Checks if Bluetooth is supported on the device.
        if (MenuActivity.mBluetoothAdapter == null) {
            Toast.makeText(mContext, "Bluetooth Low Energy not supported", Toast.LENGTH_SHORT).show();
            Log.i("seo","Bluetooth Low Energy not supported");
        }
        scanLeDevice(true);
        return view;
    }



    private void scanLeDevice(final boolean enable) {
        if(MenuActivity.mBluetoothLeScanner == null){
            Toast.makeText(mContext,"블루투스, 지도를 활성화해주세요.",Toast.LENGTH_SHORT).show();
        }
        else{
            if (enable) {
                try{
                    ScanFilter beaconFilter = new ScanFilter.Builder()
                            .build();
                    ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();

                    ScanSettings settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();

                    // Stops scanning after a pre-defined scan period.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScanning = false;
                            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            (MenuActivity.mBluetoothLeScanner).stopScan(mScanCallback);
                        }
                    }, SCAN_PERIOD);

                    mScanning = true;
                    (MenuActivity.mBluetoothLeScanner).startScan(filters, settings, mScanCallback);
                }
                catch (Exception e){
                    Log.i("seo","e : " + e);
                }

            } else {
                mScanning = false;
                (MenuActivity.mBluetoothLeScanner).stopScan(mScanCallback);
            }
        }

    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("seo", "onScanResult");
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i("seo", "onBatchScanResults: "+results.size()+" results");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("seo", "LE Scan Failed: "+errorCode);
        }

        private void processResult(ScanResult result) {
            /*
             * Create a new beacon from the list of obtains AD structures
             * and pass it up to the main thread
             */

            Log.i("seo", "Device : " + result.getDevice().getName());
            if(result.getDevice().getName()!=null) {
                if(result.getDevice().getName().contains("SHOE MONITOR")) {
                    Log.i("seo", "Device : " + result.getDevice().getName());
                    mDeviceManager.addDevice(result.getDevice());
                }
            }
        }
    };

    public View.OnClickListener mButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i("seo","MenuActivity.mBluetoothLeScanner : " + MenuActivity.mBluetoothLeScanner);
            if (MenuActivity.mBluetoothLeScanner == null){
                Toast.makeText(mContext,"블루투스, 지도를 활성화해주세요.",Toast.LENGTH_SHORT).show();
            }
            else{
                (MenuActivity.mBluetoothLeScanner).stopScan(mScanCallback);

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("message");
                builder.setMessage("연결성공");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                switch (view.getId()) {
                    case R.id.btn_LeftBle :
                        if(mDeviceManager.getDevice("L") != null){
                            (MenuActivity.mBluetoothBridge).mService.connect(mDeviceManager.getDevice("L").getAddress());
                            builder.show();
                        }
                        else{
                            Toast.makeText(mContext,"모듈을 찾을수 없습니다.",Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case R.id.btn_RightBle :
                        if(mDeviceManager.getDevice("R") != null){
                            (MenuActivity.mBluetoothBridge).mService.connect(mDeviceManager.getDevice("R").getAddress());
                            builder.show();
                        }
                        else{
                            Toast.makeText(mContext,"모듈을 찾을수 없습니다.",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            }

    };
}
