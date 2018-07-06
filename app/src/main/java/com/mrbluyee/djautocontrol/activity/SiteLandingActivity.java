package com.mrbluyee.djautocontrol.activity;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.DJSDKApplication;
import com.mrbluyee.djautocontrol.application.FPVActivity;
import com.mrbluyee.djautocontrol.application.PictureHandle;
import com.mrbluyee.djautocontrol.application.RemoteControlApplication;
import com.mrbluyee.djautocontrol.utils.StringHandleUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.thirdparty.rx.Observable;
import dji.thirdparty.rx.Subscription;
import dji.thirdparty.rx.functions.Action1;
import dji.thirdparty.rx.schedulers.Schedulers;

public class SiteLandingActivity extends FPVActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = SiteLandingActivity.class.getName();
    private RemoteControlApplication remotecontrol= new RemoteControlApplication (this);
    private PictureHandle picturehandle = new PictureHandle(this);
    private ImageView mTrackingImage1;
    private ImageView mTrackingImage2;
    private Rect[] targets1Array = null;
    private Rect[] targets2Array = null;
    private ToggleButton mAutolandBtn;
    private MyHandler1 myHandler1;
    private MyHandler2 myHandler2;
    private Timer mtimer = null;
    private TimerTask autoLandTask = null;
    private TextView mPushTv;

    private float best_focus_x = 0.5f; //最佳的绿框位置
    private float best_focus_y = 0.5f;
    private float best_inside_focus_x = 0.5f; //最佳的圆框位置
    private float best_inside_focus_y = 0.5f;

    private float targets1_center_x = 0; //绿框中心像素位置
    private float targets1_center_y = 0;
    private float recog_area = 0; //绿框面积
    private float targets1_percent_center_x = 0; //绿框中心百分比位置
    private float targets1_percent_center_y = 0;
    private  boolean targets1_land_flag = true;
    private  boolean targets1_detected = false;
    private int targets1_notdetected_num = 0;
    private  boolean target1_update = false;

    private float targets2_center_x = 0; //圆框中心像素位置
    private float targets2_center_y = 0;
    private float targets2_percent_center_x = 0; //圆框中心百分比位置
    private float targets2_percent_center_y = 0;
    private  boolean targets2_land_flag = true;
    private  boolean targets2_detected = false;
    private int targets2_notdetected_num = 0;
    private  boolean target2_update = false;
    private double angle_du = 0; //圆框相对于绿框的角度

    private float present_location_x = 0; //无人机当前预测位置
    private float present_location_y = 0;
    private float flight_offset_x = 0; //飞行偏移量
    private float flight_offset_y = 0;
    private int last_move_front = 0; //上一次的移动方向,0为无动作，1为向前，2为向后
    private int last_move_side = 0; //上一次的移动方向，0为无动作，1为向左，2为向右
    private boolean predict_mode = false;
    private int predict_time = 0;//连续预测执行的次数，超过设定次即数认为无人机镜头已偏离目标
    private int predict_round = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sitelanding);
        super.onCreate(savedInstanceState);
        initUI();
        myHandler1 = new MyHandler1();
        myHandler2 = new MyHandler2();
    }

    class MyHandler1 extends Handler {
        public MyHandler1() {
        }

        public MyHandler1(Looper L) {
            super(L);
        }
        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            targets1Array = (Rect[]) b.getSerializable("picturedetector1");
            if(targets1Array.length > 0){
                SiteLandingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage1.setX(targets1Array[0].x);
                        mTrackingImage1.setY(targets1Array[0].y);
                        mTrackingImage1.getLayoutParams().width = targets1Array[0].width;
                        mTrackingImage1.getLayoutParams().height = targets1Array[0].height;
                        mTrackingImage1.requestLayout();
                        mTrackingImage1.setVisibility(View.VISIBLE);
                        showinfo();
                        target1_update = true;
                    }
                });
            }else {
                SiteLandingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage1.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    class MyHandler2 extends Handler {
        public MyHandler2() {
        }

        public MyHandler2(Looper L) {
            super(L);
        }
        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            targets2Array = (Rect[]) b.getSerializable("picturedetector2");
            if(targets2Array.length > 0){
               SiteLandingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage2.setX(targets2Array[0].x);
                        mTrackingImage2.setY(targets2Array[0].y);
                        mTrackingImage2.getLayoutParams().width = targets2Array[0].width;
                        mTrackingImage2.getLayoutParams().height = targets2Array[0].height;
                        mTrackingImage2.requestLayout();
                        mTrackingImage2.setVisibility(View.VISIBLE);
                        showinfo();
                        target2_update = true;
                    }
                });
            }else {
                SiteLandingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage2.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }

    private void startTimer(){
        if(mtimer == null){
            mtimer = new Timer();
        }
        if(autoLandTask == null){
            autoLandTask = new TimerTask() {
                @Override
                public void run() {
                    autoLand();
                }
            };
        }
        if((mtimer != null)&&(autoLandTask != null)){
            mtimer.scheduleAtFixedRate(autoLandTask,2000,2000);
        }
    }

    private void stopTimer(){
        if (mtimer != null) {
            mtimer.cancel();
            mtimer = null;
        }
        if(autoLandTask != null){
            autoLandTask.cancel();
            autoLandTask = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //load OpenCV engine and init OpenCV library
        if(!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, getApplicationContext(), mLoaderCallback);
            Log.d(TAG, "Internal OpenCV library not found. Using Opencv Manager for initialization");
        }else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        remotecontrol.DisableVirtualStick();
    }

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            picturehandle.onManagerConnected(status);
        }
    };

    private void initUI() {
        mTrackingImage1 = (ImageView) findViewById(R.id.landing_tracking_send_rect);
        mTrackingImage2 = (ImageView) findViewById(R.id.landing_tracking_small_rect);
        mPushTv = (TextView)findViewById(R.id.sitelanding_push_tv);
        mAutolandBtn = (ToggleButton) findViewById(R.id.auto_land_btn);
        mAutolandBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    remotecontrol.EnableVirtualStick();
                    startTimer();

                } else {
                    remotecontrol.DisableVirtualStick();
                    stopTimer();
                }
            }
        });
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        super.onSurfaceTextureUpdated(surface);
        Bitmap bitmap = mVideoSurface.getBitmap();
        picturehandle.new Picture_Detector1(bitmap,myHandler1).begin();
        picturehandle.new Picture_Detector2(bitmap,myHandler2).begin();
    }

    private void setResultToToast(final String string) {
        SiteLandingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SiteLandingActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResultToText(final String string) {
        if (mPushTv == null) {
            setResultToToast("Push info tv has not be init...");
        }
        SiteLandingActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPushTv.setText(string);
            }
        });
    }

    public void showinfo(){
        StringBuffer sb = new StringBuffer();
        android.graphics.Rect parent_rect = new android.graphics.Rect();
        mVideoSurface.getDrawingRect(parent_rect);
        if(targets1Array != null){
            if(targets1Array.length>0) {
                targets1_center_x = targets1Array[0].x + targets1Array[0].width / 2;
                targets1_center_y = targets1Array[0].y + targets1Array[0].height / 2;
                recog_area = (targets1Array[0].width * targets1Array[0].height);
                targets1_percent_center_x = targets1_center_x / parent_rect.width();
                targets1_percent_center_y = targets1_center_y / parent_rect.height();
                StringHandleUtil.addLineToSB(sb, "ScreenWidth", parent_rect.width());
                StringHandleUtil.addLineToSB(sb, "ScreenHeight", parent_rect.height());
                StringHandleUtil.addLineToSB(sb, "RecogCenter_x", targets1_center_x);
                StringHandleUtil. addLineToSB(sb, "RecogCenter_y", targets1_center_y);
                StringHandleUtil. addLineToSB(sb, "RecogArea", recog_area);
                StringHandleUtil.addLineToSB(sb, "Focus_x", targets1_percent_center_x);
                StringHandleUtil.addLineToSB(sb, "Focus_y", targets1_percent_center_y);
            }
        }
        if(targets2Array != null){
            if(targets2Array.length>0){
                targets2_center_x = targets2Array[0].x + targets2Array[0].width / 2;
                targets2_center_y = targets2Array[0].y + targets2Array[0].height / 2;
                targets2_percent_center_x = targets2_center_x / parent_rect.width();
                targets2_percent_center_y = targets2_center_y / parent_rect.height();
                double delta_x = targets2_center_x - targets1_center_x;
                double delta_y = targets2_center_y - targets1_center_y;
                double distance = Math.sqrt(Math.pow(delta_x, 2)+Math.pow(delta_y, 2));
                double angle = Math.asin(delta_y / distance);
                angle_du = Math.toDegrees(angle);
                if (delta_y >= 0){
                    if(delta_x < 0) angle_du = 180 - angle_du;
                }else {
                    if(delta_x < 0) angle_du = -180 - angle_du;
                }
                StringHandleUtil.addLineToSB(sb, "InsideRecogCenter_x", targets2_center_x);
                StringHandleUtil.addLineToSB(sb, "InsideRecogCenter_y", targets2_center_y);
                StringHandleUtil.addLineToSB(sb, "InsideFocus_x", targets2_percent_center_x );
                StringHandleUtil.addLineToSB(sb, "InsideFocus_y", targets2_percent_center_y);
                StringHandleUtil.addLineToSB(sb, "angle", angle_du);
            }
        }
        setResultToText(sb.toString());
    }

    public void Target1Handle(){
        if (target1_update) { //如果目标识别有更新
            target1_update = false;
            predict_mode = false;
            predict_time = 0;//启动预测次数清零
            predict_round = 0;
            targets1_notdetected_num = 0;
            targets1_detected = true;
            flight_offset_x = targets1_percent_center_x - present_location_x;
            flight_offset_y = targets1_percent_center_y - present_location_y;
            present_location_x = targets1_percent_center_x;//修正当前位置
            present_location_y = targets1_percent_center_y;
            float x_different = present_location_x - best_focus_x;
            float y_different = present_location_y - best_focus_y;
            targets1_land_flag = true;
            if (x_different > 0.1) { // 往右偏了
                if(remotecontrol.move_finished) {
                    remotecontrol.right_move(0.15f, 1000);
                    present_location_x -= 0.1;
                    last_move_side = 2;//记录移动方向
                    targets1_land_flag = false;
                    Log.d(TAG, "Target1 left move");
                }
            } else if (x_different < -0.1) { //往左偏了
                if(remotecontrol.move_finished) {
                    remotecontrol.left_move(0.15f, 1000);
                    present_location_x += 0.1;
                    last_move_side = 1;
                    targets1_land_flag = false;
                    Log.d(TAG, "Target1 right move");
                }
            }else {
                last_move_side = 0;
            }
            if (y_different > 0.1) { //往下偏了
                if(remotecontrol.move_finished) {
                    remotecontrol.back_move(0.15f, 500);
                    present_location_y -= 0.1;
                    last_move_front = 2;
                    targets1_land_flag = false;
                    Log.d(TAG, "Target1 ahead move");
                }
            } else if (y_different < -0.1) {  //往前偏了
                if(remotecontrol.move_finished) {
                    remotecontrol.ahead_move(0.18f, 1200);
                    present_location_y += 0.1;
                    last_move_front = 1;
                    targets1_land_flag = false;
                    Log.d(TAG, "Target1 back move");
                }
            }else {
                last_move_front = 0;
            }
            if(targets1_land_flag) {
                remotecontrol.quickDown(0.5f,500);
            }else {
                remotecontrol.Down(0.08f,300);
            }
        }else {
            if ((targets1_percent_center_x != 0) || (targets1_percent_center_y != 0)) {
                targets1_notdetected_num++;
                if (targets1_notdetected_num > 2) {
                    targets1_detected = false;
                } /*else {
                    float x_different = targets1_percent_center_x - best_focus_x;
                    float y_different = targets1_percent_center_y - best_focus_y;
                    targets1_land_flag = true;
                    if (x_different > 0.1) { // 往右偏了
                        if(remotecontrol.move_finished) {
                            remotecontrol.right_move(0.08f, 1000);
                            present_location_x -= 0.1;
                            last_move_side = 2;//记录移动方向
                            targets1_land_flag = false;
                            Log.d(TAG, "Target1 left move");
                        }
                    } else if (x_different < -0.1) { //往左偏了
                        if(remotecontrol.move_finished) {
                            remotecontrol.left_move(0.16f, 1500);
                            present_location_x += 0.1;
                            last_move_side = 1;
                            targets1_land_flag = false;
                            Log.d(TAG, "Target1 right move");
                        }
                    } else {
                        last_move_side = 0;
                    }
                    if (y_different > 0.1) { //往下偏了
                        if(remotecontrol.move_finished) {
                            remotecontrol.back_move(0.08f, 1000);
                            present_location_y -= 0.1;
                            last_move_front = 2;
                            targets1_land_flag = false;
                            Log.d(TAG, "Target1 ahead move");
                        }
                    } else if (y_different < -0.1) {  //往前偏了
                        if(remotecontrol.move_finished) {
                            remotecontrol.ahead_move(0.16f, 1500);
                            present_location_y += 0.1;
                            last_move_front = 1;
                            targets1_land_flag = false;
                            Log.d(TAG, "Target1 back move");
                        }
                    } else {
                        last_move_front = 0;
                    }
                }*/
            }
        }
    }

    public void Target2Handle(){
        if(target2_update) { //识别到圆准心目标
            target2_update = false;
            predict_mode = false;
            predict_time = 0;//启动预测次数清零
            predict_round = 0;
            targets2_notdetected_num = 0;
            targets2_detected = true;
            float x_different = targets2_percent_center_x - best_inside_focus_x;
            float y_different = targets2_percent_center_y - best_inside_focus_y;
            targets2_land_flag = true;
            if (x_different > 0.1) { // 往右偏了
                remotecontrol.right_move(0.2f,700);
                present_location_x -= 0.1;
                last_move_side = 2;//记录移动方向
                targets2_land_flag  = false;
                Log.d(TAG, "Target2 left move");
            } else if (x_different < -0.1) { //往左偏了
                remotecontrol.left_move(0.10f,500);
                present_location_x += 0.1;
                last_move_side = 1;
                targets2_land_flag  = false;
                Log.d(TAG, "Target2 right move");
            } else {
                last_move_side = 0;
            }
            if (y_different > 0.1) { //往下偏了
                remotecontrol.back_move(0.08f,500);
                present_location_y -= 0.1;
                last_move_front = 2;
                targets2_land_flag  = false;
                Log.d(TAG, "Target2 ahead move");
            } else if (y_different < -0.1) {  //往前偏了
                remotecontrol.ahead_move(0.16f,700);
                present_location_y += 0.1;
                last_move_front = 1;
                targets2_land_flag  = false;
                Log.d(TAG, "Target2 back move");
            } else {
                last_move_front = 0;
            }
            if(targets2_land_flag) {
                if((y_different<0.1)&&(y_different>-0.1)&&(x_different<0.1)&&(x_different>-0.1)) {
                    remotecontrol.Down(0.08f, 500);
                    if (targets2_land_flag) {
                        DJSDKApplication.getAircraftInstance().getFlightController().
                                startLanding(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                    }
                                });
                    }
                }
            }
        }else {
            if ((targets2_percent_center_x != 0) || (targets2_percent_center_y != 0)) {
                targets2_notdetected_num++;
                if (targets2_notdetected_num > 2) {
                    targets2_detected = false;
                }
            }
        }
    }

    public void  predictHandle(){ //当检测不到图像时进入预测模式
        if(predict_mode){
            switch (predict_round % 4) {
                case 0:
                    if(remotecontrol.move_finished){
                        remotecontrol.ahead_move(0.25f,1500*(predict_round/4 + 1));
                        predict_round ++;
                    }
                    break;
                case 1:
                    if(remotecontrol.move_finished){
                        remotecontrol.right_move(0.3f,1500*(predict_round/4 + 1));
                        predict_round ++;
                    }
                    break;
                case 2:
                    if(remotecontrol.move_finished){
                        remotecontrol.back_move(0.15f,1000*(predict_round/4 + 1));
                        predict_round ++;
                    }
                    break;
                case 3:
                    if(remotecontrol.move_finished){
                        remotecontrol.left_move(0.25f,1000*(predict_round/4 + 1));
                        predict_round ++;
                    }
                    break;
            }
        }
    }

    public void angleHandle(){
        if(angle_du!=0){ // 角度更新
            if((-92<angle_du)&&(angle_du<-74)){ //正确的角度范围

            }
            else {
                if(angle_du>0){
                    remotecontrol.turn_left(100);
                    Log.d(TAG, "turn left");
                }
                else {
                    remotecontrol.turn_right(100);
                    Log.d(TAG, "turn right");
                }
            }
            angle_du = 0;
        }
    }

    public void autoLand(){
        Log.d(TAG, "auto land start");
        if((targets1_detected == false)&&(targets2_detected == false)) { //两个目标都没检测到
            predict_time++;
        }
       // if(targets2_detected == false) { //检测到第二目标后，第一目标不响应
            Target1Handle();
        //}
        Target2Handle();
        if(predict_time > 5){  //开启预判模式
            predict_mode = true;
        }
        predictHandle();
        //angleHandle();
    }
}
