package com.mrbluyee.djautocontrol.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.DJSDKApplication;

import dji.common.battery.BatteryState;
import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;


public class PushDataActivity extends Activity {

    private TextView a,b,c,d,m,n;
    private int charge,vol,cur;
    private double lat,lon;
    private float tem;
    private String status;
    private FlightController mFlightController;
    private static final String TAG = PushDataActivity.class.getName();
    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {

            DJSDKApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
                @Override
                public void onUpdate(BatteryState djiBatteryState) {
                    charge=djiBatteryState.getChargeRemainingInPercent();
                    vol=djiBatteryState.getVoltage();
                    cur=djiBatteryState.getCurrent();
                    tem=djiBatteryState.getTemperature();


                }

            });
            BaseProduct product = DJSDKApplication.getProductInstance();
            if (product != null && product.isConnected()) {
                if (product instanceof Aircraft) {
                    mFlightController = ((Aircraft) product).getFlightController();
                }
            }
            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState djiFlightState) {
                  lat=djiFlightState.getAircraftLocation().getLatitude();
                  lon=djiFlightState.getAircraftLocation().getLongitude();


                }

            });
            status=cur>0?"充电中！":"放电中";
            a.setText("电池电量： "+charge+"%\n");
            b.setText("当前电压： "+vol+"mV\n");
            c.setText("当前电流： "+cur+"mA\n");
            d.setText("电池温度： "+tem+"摄氏度\n");
            m.setText("充电状态： "+status+"\n");
            n.setText("经度："+lon+"  纬度："+lat);
            handler.postDelayed(this, 100);
        }
//        @Override
//        public void run() {
//
//           DJSDKApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
//                @Override
//               public void onUpdate(BatteryState djiBatteryState) {
//                   stringBuffer.delete(0, stringBuffer.length());
//
//                   stringBuffer.append("电池电量: ").
//                            append(djiBatteryState.getChargeRemainingInPercent()).
//                            append("%\n").append("\n");
//                    stringBuffer.append("当前电压: ").
//                            append(djiBatteryState.getVoltage()).append("mV\n").append("\n");
//                    stringBuffer.append("当前电流: ").
//                            append(djiBatteryState.getCurrent()).append("mA\n").append("\n");
//                    stringBuffer.append("电池温度: ").
//                            append(djiBatteryState.getTemperature()).append("摄氏度\n").append("\n");
//                    stringBuffer.append("充电状态: ");
//                    if(djiBatteryState.getCurrent()>0){
//                        stringBuffer.append("充电中！");
//                    }else {
//                        stringBuffer.append("放电中");
//                    }
//
//                 // a.setText(stringBuffer);
//                }
//
//            });
//           a.setText(stringBuffer);
//            handler.postDelayed(this, 100);
//        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_show_osd);
        initUI();
        handler.postDelayed(runnable, 100);
        //a.setText(stringBuffer);
    }
    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();


    }


//    public void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        //a.setText("hao");
//        try {
//            DJSDKApplication.getProductInstance().getBattery().setStateCallback(new BatteryState.Callback() {
//                @Override
//                public void onUpdate(BatteryState djiBatteryState) {
//                    stringBuffer.delete(0, stringBuffer.length());
//
//                    stringBuffer.append("电池电量: ").
//                            append(djiBatteryState.getChargeRemainingInPercent()).
//                            append("%\n").append("\n");
//                    stringBuffer.append("当前电压: ").
//                            append(djiBatteryState.getVoltage()).append("mV\n").append("\n");
//                    stringBuffer.append("当前电流: ").
//                            append(djiBatteryState.getCurrent()).append("mA\n").append("\n");
//                    stringBuffer.append("电池温度: ").
//                            append(djiBatteryState.getTemperature()).append("摄氏度\n").append("\n");
//                    stringBuffer.append("充电状态: ");
//                    if(djiBatteryState.getCurrent()>0){
//                        stringBuffer.append("充电中！");
//                    }else {
//                        stringBuffer.append("放电中");
//                    }
//
//
//                }
//            });
//        } catch (Exception ignored) {
//
//        }
//    }
//
//
//    public void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//
//        try {
//            DJSDKApplication.getProductInstance().getBattery().setStateCallback(null);
//        } catch (Exception ignored) {
//
//        }
//    }

    private void initUI(){

        a=(TextView) findViewById(R.id.texta);
        b=(TextView) findViewById(R.id.textb);
        c=(TextView) findViewById(R.id.textc);
        d=(TextView) findViewById(R.id.textd);
        m=(TextView) findViewById(R.id.textm);
        n=(TextView) findViewById(R.id.textn);

    }



}



// package com.mrbluyee.djautocontrol;
//
//import android.app.Activity;
//import android.app.Service;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
///**
// * Created by dji on 15/12/28.
// */
//public abstract class BasePushDataView extends Activity {
//
//    protected StringBuffer stringBuffer;
//
//    protected TextView textViewOSD;
//
//    public BasePushDataView(Context context) {
//        super(context);
//        init(context);
//    }
//
//    @Override
//    public void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        stringBuffer = new StringBuffer();
//    }
//
//
//
//
//    private void init(Context context) {
//        setClickable(true);
//        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
//
//        layoutInflater.inflate(R.layout.view_show_osd, this, true);
//
//        textViewOSD = (TextView) findViewById(R.id.text_show_osd);
//
//    }
//
//    protected void showStringBufferResult() {
//        post(new Runnable() {
//            @Override
//            public void run() {
//                textViewOSD.setText(stringBuffer.toString());
//            }
//        });
//    }
//}
