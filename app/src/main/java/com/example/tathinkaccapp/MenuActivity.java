package com.example.tathinkaccapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.tathinkaccapp.BLE.BleDataTransferService;
import com.example.tathinkaccapp.BLE.BluetoothBridge;
import com.example.tathinkaccapp.Dialog.BluetoothDialog;
import com.example.tathinkaccapp.Util.LocationManage;
import com.example.tathinkaccapp.Util.MpChart;
import com.example.tathinkaccapp.Util.Stopwatch;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

public class MenuActivity extends AppCompatActivity {
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothLeScanner mBluetoothLeScanner;
    public static BluetoothBridge mBluetoothBridge = BluetoothBridge.getInstance();
    private AppCompatImageView[] forkList = new AppCompatImageView[10];
    private AppCompatImageView left_foot, right_foot, img_animation;
    private DonutProgress probability_donut;
    private Switch bleSwitch;
    private TextView btn_start, txt_walk, txt_info_01, txt_info_02;
    private Stopwatch stopWatch = new Stopwatch();
    private MpChart mStepChart;
    private BarChart stepChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        probability_donut = (DonutProgress)findViewById(R.id.probability_donut);
        btn_start = (TextView)findViewById(R.id.btn_start);
        txt_walk = (TextView)findViewById(R.id.txt_walk);
        txt_info_01 = (TextView)findViewById(R.id.txt_info_01);
        txt_info_02 = (TextView)findViewById(R.id.txt_info_02);

        left_foot = (AppCompatImageView)findViewById(R.id.img_left_foot);
        right_foot = (AppCompatImageView)findViewById(R.id.img_right_foot);
        left_foot.setBackgroundColor(Color.parseColor("#00ec6865"));
        right_foot.setBackgroundColor(Color.parseColor("#00ec6865"));
        btn_start.setOnClickListener(new ButtonClickListener());
        for(int i = 0; i < forkList.length; i++){
            int getID = this.getResources().getIdentifier("img_fork_0"+i,"id",this.getPackageName());
            forkList[i] = (AppCompatImageView)findViewById(getID);
        }
        img_animation = (AppCompatImageView) findViewById(R.id.gif_rabbit);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(img_animation);

        bluetoothDevicePairingInit();
        LocationManage locationManage = new LocationManage(MenuActivity.this);
        if(!locationManage.isLocation())
            locationManage.setEnableLocation();

        // chart init
        stepChart = (BarChart)findViewById(R.id.stepChart);
        mStepChart = new MpChart(stepChart, getApplicationContext());
        mStepChart.chartInit();
        mStepChart.setWalkEntry(0, 0);

        // stopwatch init
        bleSwitch = (Switch)findViewById(R.id.bleSwitch);
        bleSwitch.setOnCheckedChangeListener(new SwitchListener());
        stopWatch.setListener(new StopwatchListener());
    }

    class SwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
            // Connect BLE
            if (state) {
                new BluetoothDialog().show(getSupportFragmentManager(), "Dialog");
            }
            else{
                mBluetoothBridge.mService.disconnect();
            }
        }
    }

    class StopwatchListener implements Stopwatch.StopWatchListener{
        @Override
        public void onTick(String time) {
            btn_start.setText(time);
            getRealTimeSpeed();
        }
    }
    int spd_counting = 0;
    int spd_leftStep = 0;
    int spd_rightStep = 0;
    void getRealTimeSpeed(){
        if(txt_info_02.getText().toString().equals("")){
            txt_info_01.setText("속도를 체크중입니다.." + (3-spd_counting));
        }
        if(spd_counting > 2){
            int distance_km = (int)((spd_counting+spd_leftStep) * 0.4);
            float ms = (float)distance_km/spd_counting;
            if(ms > 0.8){
                txt_info_01.setText("우리아이는 지금 \n'토끼' 와 같은 속도로 \n걷고있어요.");
                txt_info_02.setText("'토끼'는 100m 가는데 \n약 20초가 걸려요!");
                Glide.with(this).load(R.drawable.ic_gif_rabbit).into(img_animation);
            }
            else if(ms <= 0.8 && ms > 0.4){
                txt_info_01.setText("우리아이는 지금 \n'거북이' 와 같은 속도로 \n걷고있어요.");
                txt_info_02.setText("'거북이'는 10m 가는데 \n약 33초가 걸려요!");
                Glide.with(this).load(R.drawable.ic_gif_turtle).into(img_animation);
            }
            else if(ms <= 0.4){
                txt_info_01.setText("아이가 자고있어요");
                txt_info_02.setText("움직임이 감지되지 않습니다.");
                img_animation.setImageResource(R.drawable.ic_img_animal);
            }
            spd_leftStep = 0;
            spd_rightStep = 0;
            spd_counting = 0;
        }
        else{
            spd_counting++;
        }
    }

    private boolean isStart = false;
    class ButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_start :
                    isStart = !isStart;
                    if(isStart){
                        if(mBluetoothBridge.mService.isConnected()) {
                            mBluetoothBridge.mService.sendCommand(0x53, null);
                            mBluetoothBridge.mService.R_sendCommand(0x53, null);
                            stopWatch.start();
                        }
                        else{
                            Toast.makeText(MenuActivity.this,"블루투스를 먼저 연결해주세요.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        btn_start.setText("START");
                        mBluetoothBridge.mService.sendCommand(0x50, null);
                        mBluetoothBridge.mService.R_sendCommand(0x50, null);
                        stopWatch.stop();
                        setDataInit();
                    }
                    break;
            }
        }
    }
    private void bluetoothDevicePairingInit() {
        mBluetoothBridge.mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        ble_service_init();
    }

    private void ble_service_init() {
        Intent bindIntent = new Intent(this, BleDataTransferService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    //UART service connected/disconnected
    public ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mBluetoothBridge.mService = ((BleDataTransferService.LocalBinder) rawBinder).getService();
            Log.d("seo", "onServiceConnected mService= " + mBluetoothBridge.mService);
            if (!mBluetoothBridge.mService.initialize()) {
                Log.e("seo", "Unable to initialize Bluetooth");
                finish();
            }
        }
        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mBluetoothBridge.mService = null;
        }
    };

    public class checkSvmToStep {
        int leftStep = 0;
        int rightStep = 0;
        boolean L_flag = false;
        boolean R_flag = true;
        double tempLeftSVM = 0;
        double tempRightSVM = 0;


        public boolean isLeftStep(double svm){
            tempLeftSVM = svm;
            if(svm > 2){ // @SEO 미세조절을 시도하려면 숫자 바꾸기
                if(L_flag){
                    L_flag = !L_flag;
                    return true;
                }
                else{
                    L_flag = !L_flag;
                    return false;
                }
            }
            else{
                return false;
            }
        }
        public boolean isRightStep(double svm){
            tempRightSVM = svm;
            if(svm > 2){ // @SEO 미세조절을 시도하려면 숫자 바꾸기
                if(R_flag){
                    R_flag = !R_flag;
                    return true;
                }
                else{
                    R_flag = !R_flag;
                    return false;
                }
            }
            else{
                return false;
            }
        }
    }
    checkSvmToStep isStep = new checkSvmToStep();
    public final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BleDataTransferService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mBluetoothBridge.mMtuRequested = false;
                        new CountDownTimer(1000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }
                            @Override
                            public void onFinish() {
                                mBluetoothBridge.mConnectionSuccess = true;
                            }
                        }.start();
                    }
                });
            }

            //*********************//
            if (action.equals(BleDataTransferService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("seo", "UART_DISCONNECT_MSG");
//                        bleStateImageChange();
                    }
                });
            }

            //*********************//
            if (action.equals(BleDataTransferService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mBluetoothBridge.mService.enableTXNotification();
                //mBluetoothBridge.mService.sendCommand(BluetoothBridge.BleCommand.GetBleParams.ordinal(), null);
            }

            //*********************//
            if (action.equals(BleDataTransferService.ACTION_DATA_AVAILABLE) && isStart) {

                final byte[] txValue = intent.getByteArrayExtra(BleDataTransferService.EXTRA_DATA);

                if (txValue.length == 18) { // 오른쪽
                    short acc_x_MSB = (short) ((short)(txValue[0]) & 0x00ff);
                    short acc_x_LSB = (short) ((short)(txValue[1]) & 0x00ff);
                    short acc_y_MSB = (short) ((short)(txValue[2]) & 0x00ff);
                    short acc_y_LSB = (short) ((short)(txValue[3]) & 0x00ff);
                    short acc_z_MSB = (short) ((short)(txValue[4]) & 0x00ff);
                    short acc_z_LSB = (short) ((short)(txValue[5]) & 0x00ff);
                    short gyro_x_MSB = (short) ((short)(txValue[6]) & 0x00ff);
                    short gyro_x_LSB = (short) ((short)(txValue[7]) & 0x00ff);
                    short gyro_y_MSB = (short) ((short)(txValue[8]) & 0x00ff);
                    short gyro_y_LSB = (short) ((short)(txValue[9]) & 0x00ff);
                    short gyro_z_MSB = (short) ((short)(txValue[10]) & 0x00ff);
                    short gyro_z_LSB = (short) ((short)(txValue[11]) & 0x00ff);
                    short mag_x_MSB = (short) ((short)(txValue[12]) & 0x00ff);
                    short mag_x_LSB = (short) ((short)(txValue[13]) & 0x00ff);
                    short mag_y_MSB = (short) ((short)(txValue[14]) & 0x00ff);
                    short mag_y_LSB = (short) ((short)(txValue[15]) & 0x00ff);
                    short mag_z_MSB = (short) ((short)(txValue[16]) & 0x00ff);
                    short mag_z_LSB = (short) ((short)(txValue[17]) & 0x00ff);

                    short acc_x = (short)( acc_x_MSB << 8 | acc_x_LSB );
                    short acc_y = (short)( acc_y_MSB << 8 | acc_y_LSB );
                    short acc_z = (short)( acc_z_MSB << 8 | acc_z_LSB );
                    short gyro_x = (short)( gyro_x_MSB << 8 | gyro_x_LSB );
                    short gyro_y = (short)( gyro_y_MSB << 8 | gyro_y_LSB );
                    short gyro_z = (short)( gyro_z_MSB << 8 | gyro_z_LSB );
                    short mag_x = (short)( mag_x_LSB << 8 | mag_x_MSB );
                    short mag_y = (short)( mag_y_LSB << 8 | mag_y_MSB );
                    short mag_z = (short)( mag_z_LSB << 8 | mag_z_MSB );

                    float acc_x_data = (float)(acc_x * 0.00006103515625f);
                    float acc_y_data = (float)(acc_y * 0.00006103515625f);
                    float acc_z_data = (float)(acc_z * 0.00006103515625f);
                    float gyro_x_data = (float)(gyro_x * 0.00762939453125);
                    float gyro_y_data = (float)(gyro_y * 0.00762939453125);
                    float gyro_z_data = (float)(gyro_z * 0.00762939453125);
                    float mag_x_data = (float)(mag_x * 0.5981);
                    float mag_y_data = (float)(mag_y * 0.5981);
                    float mag_z_data = (float)(mag_z * 0.5981);

                    double svm = Math.sqrt((acc_x_data*acc_x_data)+ (acc_y_data*acc_y_data) + (acc_z_data*acc_z_data));
                    if(isStep.isRightStep(svm)){
                        spd_rightStep++;
                        isStep.rightStep++;
                    }
                }
                if (txValue.length == 19) { // 왼족
                    float voltage;
                    float ph;
                    double temperature;
                    short num;
                    int[] raw_data = new int[19];

                    //temperature = (((txValue[18] & 0xFF ) << 8 | (txValue[19] & 0xFF)) >> 5);
                    //temperature = (temperature * 0.125);

                    //DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                    //String numberAsString = decimalFormat.format(temperature);


                    short acc_x_MSB = (short) ((short)(txValue[0]) & 0x00ff);
                    short acc_x_LSB = (short) ((short)(txValue[1]) & 0x00ff);
                    short acc_y_MSB = (short) ((short)(txValue[2]) & 0x00ff);
                    short acc_y_LSB = (short) ((short)(txValue[3]) & 0x00ff);
                    short acc_z_MSB = (short) ((short)(txValue[4]) & 0x00ff);
                    short acc_z_LSB = (short) ((short)(txValue[5]) & 0x00ff);
                    short gyro_x_MSB = (short) ((short)(txValue[6]) & 0x00ff);
                    short gyro_x_LSB = (short) ((short)(txValue[7]) & 0x00ff);
                    short gyro_y_MSB = (short) ((short)(txValue[8]) & 0x00ff);
                    short gyro_y_LSB = (short) ((short)(txValue[9]) & 0x00ff);
                    short gyro_z_MSB = (short) ((short)(txValue[10]) & 0x00ff);
                    short gyro_z_LSB = (short) ((short)(txValue[11]) & 0x00ff);
                    short mag_x_MSB = (short) ((short)(txValue[12]) & 0x00ff);
                    short mag_x_LSB = (short) ((short)(txValue[13]) & 0x00ff);
                    short mag_y_MSB = (short) ((short)(txValue[14]) & 0x00ff);
                    short mag_y_LSB = (short) ((short)(txValue[15]) & 0x00ff);
                    short mag_z_MSB = (short) ((short)(txValue[16]) & 0x00ff);
                    short mag_z_LSB = (short) ((short)(txValue[17]) & 0x00ff);

                    short acc_x = (short)( acc_x_MSB << 8 | acc_x_LSB );
                    short acc_y = (short)( acc_y_MSB << 8 | acc_y_LSB );
                    short acc_z = (short)( acc_z_MSB << 8 | acc_z_LSB );
                    short gyro_x = (short)( gyro_x_MSB << 8 | gyro_x_LSB );
                    short gyro_y = (short)( gyro_y_MSB << 8 | gyro_y_LSB );
                    short gyro_z = (short)( gyro_z_MSB << 8 | gyro_z_LSB );
                    short mag_x = (short)( mag_x_LSB << 8 | mag_x_MSB );
                    short mag_y = (short)( mag_y_LSB << 8 | mag_y_MSB );
                    short mag_z = (short)( mag_z_LSB << 8 | mag_z_MSB );

                    float acc_x_data = (float)(acc_x * 0.00006103515625f);
                    float acc_y_data = (float)(acc_y * 0.00006103515625f);
                    float acc_z_data = (float)(acc_z * 0.00006103515625f);
                    float gyro_x_data = (float)(gyro_x * 0.00762939453125);
                    float gyro_y_data = (float)(gyro_y * 0.00762939453125);
                    float gyro_z_data = (float)(gyro_z * 0.00762939453125);
                    float mag_x_data = (float)(mag_x * 0.5981);
                    float mag_y_data = (float)(mag_y * 0.5981);
                    float mag_z_data = (float)(mag_z * 0.5981);

                    double svm = Math.sqrt((acc_x_data*acc_x_data)+ (acc_y_data*acc_y_data) + (acc_z_data*acc_z_data));
                    if(isStep.isLeftStep(svm)){
                        spd_leftStep++;
                        isStep.leftStep++;
                    }
                }
                setData();
            }

            //*********************//
            if (action.equals(BleDataTransferService.ACTION_IMG_INFO_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(BleDataTransferService.EXTRA_DATA);

                if(txValue.length == 1) {
//                    r_TextBat.setText("Battery " + txValue[0] + " %");
                }else if(txValue.length == 2){
//                    TextBat.setText("Battery " + txValue[0] + " %");
                }
            }
            //*********************//
            if (action.equals(BleDataTransferService.DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER)){
                //showMessage("Device doesn't support UART. Disconnecting");

                mBluetoothBridge.mService.disconnect();
            }

        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleDataTransferService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleDataTransferService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleDataTransferService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleDataTransferService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BleDataTransferService.ACTION_IMG_INFO_AVAILABLE);
        intentFilter.addAction(BleDataTransferService.DEVICE_DOES_NOT_SUPPORT_IMAGE_TRANSFER);
        return intentFilter;
    }
    private void setDataInit() {
        isStep.tempLeftSVM = 0;
        isStep.tempRightSVM = 0;
        isStep.leftStep = 0;
        isStep.rightStep = 0;
        for (int i = 0; i < forkList.length; i++) {
            forkList[i].setColorFilter(Color.parseColor("#E8E8E8"));
        }
    }

    String isGIF = "";
    private void playGIF(String val){
        if(isGIF == ""){
            if(val == "rabbit") Glide.with(this).load(R.drawable.ic_gif_rabbit).into(img_animation);
            else Glide.with(this).load(R.drawable.ic_gif_turtle).into(img_animation);
            isGIF = val;
            Log.i("seo","[1] isGif : " + isGIF);
        }else{
            if(isGIF == val) return ;
            else{
                if(val == "rabbit") Glide.with(this).load(R.drawable.ic_gif_rabbit).into(img_animation);
                else Glide.with(this).load(R.drawable.ic_gif_turtle).into(img_animation);
                isGIF = val;
            }
        }
    }


    private void setData(){
        // 그래프
        mStepChart.setWalkEntry(isStep.tempLeftSVM, isStep.tempRightSVM);

        // 걸음
        txt_walk.setText(isStep.leftStep+isStep.rightStep+"");

        // 칼로리 oxygen(mL) = 3.5 * 3.8(MET) * kg * min
        int sec = (int)stopWatch.getTotalTimeElapsed() / 1000;
        double oxygenML = 3.5 * 3.8 * 70 * (sec / 60.0000);
        double Kcal = (oxygenML / 1000.000) * 5;
        try{
            if((Kcal/2) <= forkList.length)
                forkList[(int)Kcal].setColorFilter(Color.parseColor("#ec6865"));
        }catch (Exception e){}

        // probability_donut
        probability_donut.setDonut_progress(String.valueOf(Math.abs(isStep.leftStep-isStep.rightStep)));

        // probability_foot
        if(isStep.leftStep * 2 > 255 || isStep.rightStep * 2 > 255 )
            return;
        String l_alpha = Integer.toString(isStep.leftStep*2,16).length() == 1 ? "0" + Integer.toString(isStep.leftStep*2,16) : Integer.toString(isStep.leftStep*2,16);
        String r_alpha = Integer.toString(isStep.rightStep*2,16).length() == 1 ? "0" + Integer.toString(isStep.rightStep*2,16) : Integer.toString(isStep.rightStep*2,16);
        String l_color = "#" + l_alpha + "ec6865";
        String r_color = "#" + r_alpha + "ec6865";
        left_foot.setBackgroundColor(Color.parseColor(l_color));
        right_foot.setBackgroundColor(Color.parseColor(r_color));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {

        }
        unbindService(mServiceConnection);

        if(mBluetoothBridge.mService!=null) {
            mBluetoothBridge.mService.stopSelf();
            mBluetoothBridge.mService = null;
        }

    }

}
