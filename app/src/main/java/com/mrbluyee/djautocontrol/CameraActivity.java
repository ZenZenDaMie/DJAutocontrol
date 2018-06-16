package com.mrbluyee.djautocontrol;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity extends FPVActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {
    private static final String TAG = CameraActivity.class.getName();
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
        mCameraDownBtn.setOnClickListener(this);
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

                break;
            }
            case R.id.btn_camera_down:{

                break;
            }
            default:
                break;
        }
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
}

