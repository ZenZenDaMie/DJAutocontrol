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

    public float Pitch;//俯仰角(前后转动？？？)
    public float Roll;//滚动(左右转动？？？)
    public float Yaw;//Yaw轴，偏航(角速度)
    public float Throttle;//(好像是风速？？)
    public boolean move_finished = true;
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
    public void Up(float throttle,int ms){
        move_finished = false;
        if (Throttle < 50) {
            Throttle = throttle;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Throttle=0;
                move_finished = true;
            }
        }, ms);
    }
    public void Down(float throttle,int ms){
        move_finished = false;
        if (Throttle >=0) {
            Throttle = - throttle;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Throttle=0;
                move_finished = true;
            }
        }, ms);
    }
    public void quickDown(float throttle,int ms){
        move_finished = false;
        if (Throttle >=0) {
            Throttle = - throttle;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Throttle=0;
                move_finished = true;
            }
        }, ms);
    }
    public void turn_left(int ms){
        move_finished = false;
        Yaw = -10f;
        Pitch = 0;
        Roll = 0;
        Throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Yaw = 0;
                Pitch = 0;
                Roll = 0;
                Throttle = 0;
                move_finished = true;
            }
        }, ms);
    }
    public void turn_right(int ms){
        move_finished = false;
        Yaw = 10f;
        Pitch = 0;
        Roll = 0;
        Throttle = 0;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Yaw = 0;
                Pitch = 0;
                Roll = 0;
                Throttle = 0;
                move_finished = true;
            }
        }, ms);
    }
    public void left_move(float pitch,int ms){
        move_finished = false;
        Pitch = - pitch;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Pitch = 0;
                move_finished = true;
            }
        }, ms);
    }
    public void right_move(float pitch,int ms){
        move_finished = false;
        Pitch = pitch;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Pitch = 0;
                move_finished = true;
            }
        }, ms);
    }
    public void ahead_move(float roll,int ms){
        move_finished = false;
        Roll = roll;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Roll = 0;
                move_finished = true;
            }
        }, ms);
    }
    public void back_move(float roll,int ms){
        move_finished = false;
        Roll = -roll;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                Roll = 0;
                move_finished = true;
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
                        .sendVirtualStickFlightControlData(new FlightControlData(Pitch,
                                        Roll,
                                        Yaw,
                                        Throttle),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {

                                    }
                                });
            }
        }
    }
}
