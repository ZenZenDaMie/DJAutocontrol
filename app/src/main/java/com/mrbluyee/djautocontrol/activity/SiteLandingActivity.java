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
    private ImageView mTrackingImage;
    private Rect[] targetsArray = null;
    private ToggleButton mAutolandBtn;
    private MyHandler myHandler;
    private TextView mPushTv;
    private boolean auto_land_flag = false;
    private float best_focus_x = 0.5187f;
    private float best_focus_y = 0.6212f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sitelanding);
        super.onCreate(savedInstanceState);
        initUI();
        myHandler = new MyHandler();
    }


    class MyHandler extends Handler {
        public MyHandler() {
        }

        public MyHandler(Looper L) {
            super(L);
        }
        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            targetsArray = (Rect[]) b.getSerializable("picturehandle");
            if(targetsArray.length > 0){
                SiteLandingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage.setX(targetsArray[0].x);
                        mTrackingImage.setY(targetsArray[0].y);
                        mTrackingImage.getLayoutParams().width = targetsArray[0].width;
                        mTrackingImage.getLayoutParams().height = targetsArray[0].height;
                        mTrackingImage.requestLayout();
                        mTrackingImage.setVisibility(View.VISIBLE);
                        autoLand(targetsArray[0]);
                    }
                });
            }else {
                SiteLandingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage.setVisibility(View.INVISIBLE);
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
        mTrackingImage = (ImageView) findViewById(R.id.landing_tracking_send_rect);
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
        picturehandle.new Picture_Match(bitmap,myHandler).begin();
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

    public void autoLand(Rect pic){
        android.graphics.Rect parent_rect = new android.graphics.Rect();
        mVideoSurface.getDrawingRect(parent_rect);
        float pic_center_x = pic.x + pic.width/2;
        float pic_center_y = pic.y + pic.height/2;
        float recog_area = (pic.width * pic.height);
        float center_x = pic_center_x/parent_rect.width();
        float center_y = pic_center_y/parent_rect.height();
        StringBuffer sb = new StringBuffer();
        StringHandleUtil.addLineToSB(sb, "ScreenWidth", parent_rect.width());
        StringHandleUtil.addLineToSB(sb, "ScreenHeight", parent_rect.height());
        StringHandleUtil.addLineToSB(sb, "RecogCenter_x", pic_center_x);
        StringHandleUtil.addLineToSB(sb, "RecogCenter_y", pic_center_y);
        StringHandleUtil.addLineToSB(sb, "RecogArea", recog_area);
        StringHandleUtil.addLineToSB(sb, "Focus_x", center_x);
        StringHandleUtil.addLineToSB(sb, "Focus_y", center_y);
        StringHandleUtil.addLineToSB(sb, "Auto_enable", auto_land_flag);
        setResultToText(sb.toString());
        if(auto_land_flag){
            float x_different = center_x - best_focus_x;
            float y_different = center_y - best_focus_y;
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
