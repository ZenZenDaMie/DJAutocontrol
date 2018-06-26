package com.mrbluyee.djautocontrol.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mrbluyee.djautocontrol.activity.RemoteControlActivity;
import com.mrbluyee.djautocontrol.utils.ModuleVerificationUtil;

import org.opencv.android.BaseLoaderCallback;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;


/**
 * Created by rsj on 2018/6/24.
 */

public class RemoteControlApplication  extends Application {
    public Timer sendVirtualStickDataTimer;
    public SendVirtualStickDataTask sendVirtualStickDataTask;

    public float pitch;//俯仰角(前后转动？？？)
    public float roll;//滚动(左右转动？？？)
    public float yaw;//Yaw轴，偏航(角速度)
    public float throttle;//(好像是风速？？)
    public FlightControllerKey isSimulatorActived;

    private Context context;
    public RemoteControlApplication (Context context){
        this.context = context;

    }
    public void EnableVirtualStick(){
        DJSDKApplication.getAircraftInstance().
                getFlightController().
                setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        //Log.i(RemoteControlActivity.this,"asassa");
                    }
                });
        //设置飞行器坐标系和控制模式
        DJSDKApplication.getAircraftInstance().getFlightController().
                setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        DJSDKApplication.getAircraftInstance().getFlightController().
                setVerticalControlMode(VerticalControlMode.VELOCITY);
        DJSDKApplication.getAircraftInstance().getFlightController().
                setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        DJSDKApplication.getAircraftInstance().getFlightController().
                setRollPitchControlMode(RollPitchControlMode.VELOCITY);

        //定时器用来定时，200ms发送一次数据
        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
        }
    }
    public void DisableVirtualStick() {
        DJSDKApplication.getAircraftInstance().
                getFlightController().
                setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });
    }
    public void Up(int ms){
        if (throttle < 50) {
            throttle = 2;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                throttle=0;
            }
        }, ms);
    }
    public void Down(int ms){
        if (throttle >=0) {
            throttle = -2;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                throttle=0;
            }
        }, ms);
    }
    public void turn_left(int ms){
        yaw = -10f;
        pitch = 0;
        roll = 0;
        throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                yaw = 0;
                pitch = 0;
                roll = 0;
                throttle = 0;
            }
        }, ms);
    }
    public void turn_right(int ms){
        yaw = 10f;
        pitch = 0;
        roll = 0;
        throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                yaw = 0;
                pitch = 0;
                roll = 0;
                throttle = 0;
            }
        }, ms);
    }
    public void left_move(int ms){
        yaw = 0;
        pitch = -2;
        roll = 0;
        throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                yaw = 0;
                pitch = 0;
                roll = 0;
                throttle = 0;
            }
        }, ms);
    }
    public void right_move(int ms){
        yaw = 0;
        pitch = 2;
        roll = 0;
        throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                yaw = 0;
                pitch = 0;
                roll = 0;
                throttle = 0;
            }
        }, ms);
    }
    public void ahead_move(int ms){
        yaw = 0;
        pitch = 0;
        roll = 1;
        throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                yaw = 0;
                pitch = 0;
                roll = 0;
                throttle = 0;
            }
        }, ms);
    }
    public void back_move(int ms){
        yaw = 0;
        pitch = 0;
        roll = -1;
        throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                yaw = 0;
                pitch = 0;
                roll = 0;
                throttle = 0;
            }
        }, ms);
    }
    public class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            //Toast.makeText(RemoteControlActivity, "virtual_stick on", Toast.LENGTH_LONG).show();

            if (ModuleVerificationUtil.isFlightControllerAvailable()) {
                DJSDKApplication.getAircraftInstance()
                        .getFlightController()
                        .sendVirtualStickFlightControlData(new FlightControlData(pitch,
                                        roll,
                                        yaw,
                                        throttle),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                });
            }
        }
    }
}
