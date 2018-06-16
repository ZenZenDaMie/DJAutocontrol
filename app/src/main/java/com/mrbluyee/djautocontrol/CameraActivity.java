package com.mrbluyee.djautocontrol;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import dji.common.error.DJIError;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.util.CommonCallbacks;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends FPVActivity implements TextureView.SurfaceTextureListener, View.OnClickListener,View.OnTouchListener {
    private static final String TAG = CameraActivity.class.getName();
    private Timer timer;
    private GimbalRotateTimerTask gimbalRotationTimerTask;
    private Button mCaptureBtn, mCameraRiseBtn, mCameraDownBtn;
    private ToggleButton mRecordBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_camera);
        super.onCreate(savedInstanceState);
        initUI();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void initUI() {

        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mCameraRiseBtn = (Button) findViewById(R.id.btn_camera_rise);
        mCameraDownBtn = (Button) findViewById(R.id.btn_camera_down);

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
                    new FileSaver().save();
                } else {
                    new FileSaver().save();
                }
            }
        });
    }
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:{
                new FileSaver().save();
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

        public void save() {
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "drone_camera.jpg");
                file.createNewFile();

                FileOutputStream os = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(os);

                Bitmap bitmap = mVideoSurface.getBitmap();
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
}


