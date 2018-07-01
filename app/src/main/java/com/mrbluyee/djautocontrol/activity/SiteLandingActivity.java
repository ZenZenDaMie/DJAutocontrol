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
import com.mrbluyee.djautocontrol.application.FPVActivity;
import com.mrbluyee.djautocontrol.application.PictureHandle;
import com.mrbluyee.djautocontrol.application.RemoteControlApplication;
import com.mrbluyee.djautocontrol.utils.StringHandleUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

import java.util.Timer;

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
    private TextView mPushTv;
    private boolean auto_land_flag = false;
    private float best_focus_x = 0.5187f;
    private float best_focus_y = 0.6212f;
    private float targets1_center_x = 0;
    private float targets1_center_y = 0;
    private float targets1_percent_center_x = 0;
    private float targets1_percent_center_y = 0;
    private float targets2_center_x = 0;
    private float targets2_center_y = 0;
    private float targets2_percent_center_x = 0;
    private float targets2_percent_center_y = 0;
    private float recog_area = 0;
    private double angle_du = 0;
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
                    auto_land_flag = true;
                    remotecontrol.EnableVirtualStick();
                } else {
                    auto_land_flag = false;
                    remotecontrol.DisableVirtualStick();
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
        StringHandleUtil.addLineToSB(sb, "Auto_enable", auto_land_flag);
        setResultToText(sb.toString());
    }

    public void autoLand(){
        if(auto_land_flag){
            float x_different = targets1_percent_center_x  - best_focus_x;
            float y_different = targets1_percent_center_y - best_focus_y;
            boolean land_flag = true;
            if(x_different > 0.1){ // 往右偏了
                land_flag = false;
                remotecontrol.left_move(10,1000);
                Log.d(TAG, "left move");
            }else if(x_different < -0.1){ //往左偏了
                land_flag = false;
                remotecontrol.right_move(10,1000);
                Log.d(TAG, "right move");
            }
            if(y_different > 0.1){ //往下偏了
                land_flag = false;
                remotecontrol.ahead_move(10,1000);
                Log.d(TAG, "ahead move");
            }else if(y_different < -0.1){  //往前偏了
                land_flag = false;
                remotecontrol.back_move(10,1000);
                Log.d(TAG, "back move");
            }
/*            if(land_flag){

          }
*/
        }
    }
}
