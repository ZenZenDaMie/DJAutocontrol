package com.mrbluyee.djautocontrol.activity;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.DJSDKApplication;
import com.mrbluyee.djautocontrol.application.FPVActivity;
import com.mrbluyee.djautocontrol.application.PictureHandle;
import com.mrbluyee.djautocontrol.utils.ModuleVerificationUtil;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;

public class CameraActivity extends FPVActivity implements TextureView.SurfaceTextureListener, View.OnClickListener,View.OnTouchListener {
    private static final String TAG = CameraActivity.class.getName();
    private Timer timer;
    private GimbalRotateTimerTask gimbalRotationTimerTask;
    private Button mCaptureBtn, mCameraRiseBtn, mCameraDownBtn;
    private ToggleButton mRecordBtn;
    private PictureHandle picturehandle = new PictureHandle(this);
    private ImageView mTrackingImage;
    private Rect[] targetsArray = null;
    private MyHandler myHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
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
                CameraActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTrackingImage.setX(targetsArray[0].x);
                        mTrackingImage.setY(targetsArray[0].y);
                        mTrackingImage.getLayoutParams().width = targetsArray[0].width;
                        mTrackingImage.getLayoutParams().height = targetsArray[0].height;
                        mTrackingImage.requestLayout();
                        mTrackingImage.setVisibility(View.VISIBLE);
                    }
                });
            }else {
                CameraActivity.this.runOnUiThread(new Runnable() {
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
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mCameraRiseBtn = (Button) findViewById(R.id.btn_camera_rise);
        mCameraDownBtn = (Button) findViewById(R.id.btn_camera_down);
        mTrackingImage = (ImageView) findViewById(R.id.camera_tracking_send_rect);

        mCaptureBtn.setOnClickListener(this);
        mCameraRiseBtn.setOnClickListener(this);
        mCameraRiseBtn.setOnTouchListener(this);
        mCameraRiseBtn.setBackgroundColor(getColor(R.color.colorPrimary));
        mCameraDownBtn.setOnClickListener(this);
        mCameraDownBtn.setOnTouchListener(this);
        mCameraDownBtn.setBackgroundColor(getColor(R.color.colorPrimary));
        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                } else {

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:{
                Bitmap bitmap = mVideoSurface.getBitmap();
                new FileSaver(bitmap).save();
                break;
            }
            case R.id.btn_camera_rise:{
                Log.d(TAG,"camera rise pressed");
                break;
            }
            case R.id.btn_camera_down:{
                Log.d(TAG,"camera down pressed");
                break;
            }
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.btn_camera_rise) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mCameraRiseBtn.setBackgroundColor(getColor(R.color.colorPrimary));
                if (timer != null) {
                    if (gimbalRotationTimerTask != null) {
                        gimbalRotationTimerTask.cancel();
                    }
                    timer.cancel();
                    timer.purge();
                    gimbalRotationTimerTask = null;
                    timer = null;
                }
                if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                    DJSDKApplication.getProductInstance().getGimbal().
                            rotate(null, new CommonCallbacks.CompletionCallback() {

                                @Override
                                public void onResult(DJIError error) {

                                }
                            });
                }
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mCameraRiseBtn.setBackgroundColor(getColor(R.color.colorAccent));
                if (timer == null) {
                    timer = new Timer();
                    gimbalRotationTimerTask = new GimbalRotateTimerTask(10);
                    timer.schedule(gimbalRotationTimerTask, 0, 100);
                }
            }
        }
        if (v.getId() == R.id.btn_camera_down) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mCameraDownBtn.setBackgroundColor(getColor(R.color.colorPrimary));
                if (timer != null) {
                    if (gimbalRotationTimerTask != null) {
                        gimbalRotationTimerTask.cancel();
                    }
                    timer.cancel();
                    timer.purge();
                    gimbalRotationTimerTask = null;
                    timer = null;
                }
                if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                    DJSDKApplication.getProductInstance().getGimbal().
                            rotate(null, new CommonCallbacks.CompletionCallback() {

                                @Override
                                public void onResult(DJIError error) {

                                }
                            });
                }
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mCameraDownBtn.setBackgroundColor(getColor(R.color.colorAccent));
                if (timer == null) {
                    timer = new Timer();
                    gimbalRotationTimerTask = new GimbalRotateTimerTask(-10);
                    timer.schedule(gimbalRotationTimerTask, 0, 100);
                }
            }
        }
        return true;
    }

    private class FileSaver implements Runnable {
        private Bitmap bitmap;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        public FileSaver(Bitmap bitmap){
            this.bitmap = bitmap;
        }
        public void save() {
            new Thread(this).start();
        }
        @Override
        public void run() {
            try {
                Date date = new Date(System.currentTimeMillis());
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Drone_IMG_"+simpleDateFormat.format(date)+".jpg");
                file.createNewFile();

                FileOutputStream os = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(os);

                //Bitmap bitmap = mVideoSurface.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

                bos.flush();
                bos.close();
                os.close();
                showToast("photo saved");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class GimbalRotateTimerTask extends TimerTask {
        float pitchValue;

        GimbalRotateTimerTask(float pitchValue) {
            super();
            this.pitchValue = pitchValue;
        }
        @Override
        public void run() {
            if (ModuleVerificationUtil.isGimbalModuleAvailable()) {
                DJSDKApplication.getProductInstance().getGimbal().
                        rotate(new Rotation.Builder().pitch(pitchValue)
                                .mode(RotationMode.SPEED)
                                .yaw(Rotation.NO_ROTATION)
                                .roll(Rotation.NO_ROTATION)
                                .time(0)
                                .build(), new CommonCallbacks.CompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {

                            }
                        });
            }
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        super.onSurfaceTextureUpdated(surface);
        Bitmap bitmap = mVideoSurface.getBitmap();
        picturehandle.new Picture_Match(bitmap,myHandler).begin();
    }
}


