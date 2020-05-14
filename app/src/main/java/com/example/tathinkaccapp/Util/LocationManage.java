package com.example.tathinkaccapp.Util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class LocationManage {
    public static Context mContext;

    public static boolean gps_enabled = false;
    public static boolean network_enabled = false;
    public static LocationManager lm;

    public LocationManage(Context context){
        mContext = context;
        lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);

    }

    public static boolean isLocation(){
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex) { }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            return false;
        }
        else{
            return true;
        }
    }

    public static void setEnableLocation(){
        new AlertDialog.Builder(mContext)
                .setTitle("알림")
                .setMessage("지도를 활성화해주세요.")
                .setPositiveButton("활성화", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mContext.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(mContext,"지도를 활성화하지 않으면, BLE가 작동하지않습니다.",Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

}
