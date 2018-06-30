package com.mrbluyee.djautocontrol.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.DJSDKApplication;

import dji.common.battery.BatteryState;
import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;


public class PushDataActivity extends Activity {

    private TextView t,a,b,c,d,m,n,o,p;
    private int charge,vol,cur;
    private double lat,lon;
    private float tem;
    private String status,statusb;
    private BaseProduct mproduct = null;
    private FlightController mFlightController = null;
    private static final String TAG = PushDataActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dronestatus);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJSDKApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initUI();
        initFlightController();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(mproduct.isConnected()){
                statusb="连接中";
            }
            else {
                statusb="连接断开！";
            }
            updateUI();
        }
    };

    private void initUI(){
        t=(TextView) findViewById(R.id.textt);
        a=(TextView) findViewById(R.id.texta);
        b=(TextView) findViewById(R.id.textb);
        c=(TextView) findViewById(R.id.textc);
        d=(TextView) findViewById(R.id.textd);
        m=(TextView) findViewById(R.id.textm);
        n=(TextView) findViewById(R.id.textn);
        o=(TextView) findViewById(R.id.texto);
        p=(TextView) findViewById(R.id.textp);
        t.setText("\n无人机实时状态参数显示\n");
    }

    private void updateUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                a.setText("电池电量： "+charge+"%\n");
                b.setText("当前电压： "+vol+"mV\n");
                c.setText("当前电流： "+cur+"mA\n");
                d.setText("电池温度： "+tem+"摄氏度\n");
                m.setText("充电状态： "+status+"\n");
                n.setText("经度："+lon+"\n");
                o.setText("纬度："+lat+"\n");
                p.setText("连接状态："+statusb);
            }
        });
    }

    private void initFlightController() {
        mproduct = DJSDKApplication.getProductInstance();
        if (mproduct != null && mproduct.isConnected()) {
            if (mproduct instanceof Aircraft) {
                mFlightController = ((Aircraft)mproduct).getFlightController();
                mproduct.getBattery().setStateCallback(new BatteryState.Callback() {
                    @Override
                    public void onUpdate(BatteryState djiBatteryState) {
                        charge = djiBatteryState.getChargeRemainingInPercent();
                        vol = djiBatteryState.getVoltage();
                        cur = djiBatteryState.getCurrent();
                        tem = djiBatteryState.getTemperature();
                        status=cur>0?"充电中！":"放电中";
                        statusb="连接中";
                        updateUI();
                    }
                });
            }
        }
        if (mFlightController != null) {
            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(FlightControllerState
                                                     djiFlightControllerCurrentState) {
                            lat=djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            lon=djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            updateUI();
                        }
                    });
        }
    }
}
