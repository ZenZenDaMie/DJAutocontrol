package com.mrbluyee.djautocontrol.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.DJSDKApplication;
import com.mrbluyee.djautocontrol.utils.ModuleVerificationUtil;
import com.mrbluyee.djautocontrol.utils.OnScreenJoystick;
import com.mrbluyee.djautocontrol.utils.OnScreenJoystickListener;
import com.mrbluyee.djautocontrol.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;


import dji.common.flightcontroller.simulator.SimulatorState;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.Simulator;
import com.mrbluyee.djautocontrol.application.RemoteControlApplication;
/**
 * Created by rsj on 2018/6/22.
 */

public class RemoteControlActivity extends MainActivity implements View.OnClickListener{
    private static final String TAG = RemoteControlActivity.class.getName();
    private boolean yawControlModeFlag = true;

    private RemoteControlApplication remotecontrol= new RemoteControlApplication (this);
    private RemoteControlApplication.SendVirtualStickDataTask sendVirtualStickDataTask;


    private boolean rollPitchControlModeFlag = true;
    private boolean verticalControlModeFlag = true;
    private boolean horizontalCoordinateFlag = true;

    public boolean ControlFlag=false;

    private Button btnEnableVirtualStick;
    private Button btnDisableVirtualStick;
    private Button btnTakeOff;
    private Button btnAutoLand;
    private Button btnUp;
    private Button btnDown;
    private Button btnLeftTurn;
    private Button btnRightTurn;
    private Button btnLeftMove;
    private Button btnRightMove;
    private Button btnAheadMove;
    private Button btnBackMove;

    private TextView textView;


    private OnScreenJoystick screenJoystickRight;
    private OnScreenJoystick screenJoystickLeft;

    //private Timer sendVirtualStickDataTimer;

    //Ground or Body coordinate system
    //地面坐标系和机体坐标系
    private float pitch;//俯仰角(前后转动？？？)
    private float roll;//滚动(左右转动？？？)
    private float yaw;//Yaw轴，偏航(角速度)
    private float throttle;//(好像是风速？？)
    private FlightControllerKey isSimulatorActived;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remotecontrol);
        initAllKeys();
        initUI();
    }
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setUpListeners();
    }
    private void initAllKeys() {
        isSimulatorActived = FlightControllerKey.create(FlightControllerKey.IS_SIMULATOR_ACTIVE);
    }
    private void initUI() {
        //使能
        btnEnableVirtualStick = (Button) findViewById(R.id.btn_enable_virtual_stick);
        //使失能
        btnDisableVirtualStick = (Button) findViewById(R.id.btn_disable_virtual_stick);
        //水平坐标

        btnTakeOff = (Button) findViewById(R.id.btn_take_off);
        btnAutoLand = (Button) findViewById(R.id.btn_auto_land);
        btnUp=(Button)findViewById(R.id.btn_up);
        btnDown=(Button)findViewById(R.id.btn_down);
        btnLeftTurn=(Button)findViewById(R.id.btn_left_turn);
        btnRightTurn=(Button)findViewById(R.id.btn_right_turn);
        btnLeftMove=(Button)findViewById(R.id.btn_left_move);
        btnRightMove=(Button)findViewById(R.id.btn_right_move);
        btnAheadMove=(Button)findViewById(R.id.btn_ahead_move);
        btnBackMove=(Button)findViewById(R.id.btn_back_move);

        screenJoystickRight = (OnScreenJoystick) findViewById(R.id.directionJoystickRight);
        screenJoystickLeft = (OnScreenJoystick) findViewById(R.id.directionJoystickLeft);

        btnEnableVirtualStick.setOnClickListener(this);
        btnDisableVirtualStick.setOnClickListener(this);

        btnTakeOff.setOnClickListener(this);
        btnAutoLand.setOnClickListener(this);
        btnUp.setOnClickListener(this);
        btnDown.setOnClickListener(this);
        btnLeftTurn.setOnClickListener(this);
        btnRightTurn.setOnClickListener(this);
        btnLeftMove.setOnClickListener(this);
        btnRightMove.setOnClickListener(this);
        btnAheadMove.setOnClickListener(this);
        btnBackMove.setOnClickListener(this);

        Boolean isSimulatorOn = (Boolean) KeyManager.getInstance().getValue(isSimulatorActived);

    }
    private void setUpListeners() {
        Simulator simulator = ModuleVerificationUtil.getSimulator();
        if (simulator != null) {
            simulator.setStateCallback(new SimulatorState.Callback() {
                @Override
                public void onUpdate(@NonNull final SimulatorState simulatorState) {
                    ToastUtils.setResultToText(textView,
                            "Yaw : "
                                    + simulatorState.getYaw()
                                    + ","
                                    + "X : "
                                    + simulatorState.getPositionX()
                                    + "\n"
                                    + "Y : "
                                    + simulatorState.getPositionY()
                                    + ","
                                    + "Z : "
                                    + simulatorState.getPositionZ());
                }
            });
        } else {
            ToastUtils.setResultToToast("Disconnected!");
        }

        screenJoystickLeft.setJoystickListener(new OnScreenJoystickListener() {

            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }
                float pitchJoyControlMaxSpeed = 10;
                float rollJoyControlMaxSpeed = 10;

                if (horizontalCoordinateFlag) {
                    if (rollPitchControlModeFlag) {
                        remotecontrol.Pitch = (float) (pitchJoyControlMaxSpeed * pX);

                        remotecontrol.Roll = (float) (rollJoyControlMaxSpeed * pY);
                    } else {
                        remotecontrol.Pitch = -(float) (pitchJoyControlMaxSpeed * pY);

                        remotecontrol.Roll = (float) (rollJoyControlMaxSpeed * pX);
                    }
                }

                if (null == remotecontrol.sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = remotecontrol.new SendVirtualStickDataTask();
                    remotecontrol.sendVirtualStickDataTimer = new Timer();
                    remotecontrol.sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
                }
            }
        });

        screenJoystickRight.setJoystickListener(new OnScreenJoystickListener() {
            @Override
            public void onTouch(OnScreenJoystick joystick, float pX, float pY) {
                if (Math.abs(pX) < 0.02) {
                    pX = 0;
                }

                if (Math.abs(pY) < 0.02) {
                    pY = 0;
                }


                float verticalJoyControlMaxSpeed = 2;
                float yawJoyControlMaxSpeed = 3;

                remotecontrol.Yaw = yawJoyControlMaxSpeed * pX;
                remotecontrol.Throttle = verticalJoyControlMaxSpeed * pY;

                if (null == remotecontrol.sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = remotecontrol.new SendVirtualStickDataTask();
                    remotecontrol.sendVirtualStickDataTimer = new Timer();
                    remotecontrol.sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 0, 200);
                }
            }
        });
    }
    public void onClick(View v) {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_enable_virtual_stick:{
                if(ControlFlag==false){
                    Toast.makeText(this, "virtual_stick on", Toast.LENGTH_LONG).show();
                    ControlFlag=true;
                    remotecontrol.EnableVirtualStick();
                }
                else{
                    Toast.makeText(this, "virtual_stick still on", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.btn_disable_virtual_stick:{
                if(ControlFlag==true){
                    Toast.makeText(this, "virtual_stick off", Toast.LENGTH_LONG).show();
                    ControlFlag=false;
                    remotecontrol.DisableVirtualStick();
                }
                else{
                    Toast.makeText(this, "virtual_stick still off", Toast.LENGTH_LONG).show();
                }
            }
            case R.id.btn_take_off:{
                DJSDKApplication.getAircraftInstance().getFlightController()
                        .startTakeoff(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                            }
                        });
                break;
            }
            case R.id.btn_auto_land:{
                Toast.makeText(this, "startLanding", Toast.LENGTH_LONG).show();

                DJSDKApplication.getAircraftInstance().getFlightController().
                        startLanding(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                            }
                        });
                break;
            }
            case R.id.btn_up:{
                Toast.makeText(this, "start up", Toast.LENGTH_LONG).show();
                remotecontrol.Up(0.5f,1000);
                //roll=(float)(roll+1);
                break;
            }
            case R.id.btn_down:{
                Toast.makeText(this, "start down", Toast.LENGTH_LONG).show();
                remotecontrol.Down(0.5f,1000);
                break;
            }
            case R.id.btn_left_turn:{
                remotecontrol.turn_left(1000);
                Log.d(TAG, "turn left");
                break;
            }
            case R.id.btn_right_turn:{
                remotecontrol.turn_right(1000);
                Log.d(TAG, "turn right");
                break;
            }
            case R.id.btn_ahead_move:{
                remotecontrol.ahead_move(0.5f,1000);
                Log.d(TAG, "ahead move");
                break;
            }
            case R.id.btn_back_move: {
                remotecontrol.back_move(0.5f,1000);
                Log.d(TAG, "back move");
                break;
            }
            case R.id.btn_left_move:{
                remotecontrol.left_move(0.5f,1000);
                Log.d(TAG, "left move");
                break;
            }
            case R.id.btn_right_move:{
                remotecontrol.right_move(0.5f,1000);
                Log.d(TAG, "right move");
                break;
            }
        }
    }
}
