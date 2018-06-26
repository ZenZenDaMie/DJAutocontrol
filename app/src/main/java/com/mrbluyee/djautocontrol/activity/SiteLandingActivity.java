package com.mrbluyee.djautocontrol.activity;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.mrbluyee.djautocontrol.R;
import com.mrbluyee.djautocontrol.application.FPVActivity;
import com.mrbluyee.djautocontrol.application.PictureHandle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

public class SiteLandingActivity extends FPVActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = SiteLandingActivity.class.getName();

    private PictureHandle picturehandle = new PictureHandle(this);
    private ImageView mTrackingImage;
    private Rect[] targetsArray = null;
    private MyHandler myHandler;
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
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        super.onSurfaceTextureUpdated(surface);
        Bitmap bitmap = mVideoSurface.getBitmap();
        picturehandle.new Picture_Match(bitmap,myHandler).begin();
    }
}
