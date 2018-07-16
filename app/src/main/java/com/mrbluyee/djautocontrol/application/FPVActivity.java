package com.mrbluyee.djautocontrol.application;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.products.Aircraft;
import dji.sdk.useraccount.UserAccountManager;
import com.mrbluyee.djautocontrol.R;

public class FPVActivity extends Activity implements SurfaceTextureListener{

    private static final String TAG = FPVActivity.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;
    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    private BaseProduct mProduct;
    private Camera camera = null;
    //To store index chosen in PopupNumberPicker listener
    protected static int[] INDEX_CHOSEN = {-1, -1, -1};
    protected TextView mConnectStatusTextView;
    protected TextureView mVideoSurface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJSDKApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        mConnectStatusTextView = (TextView) findViewById(R.id.ConnectStatusTextView);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }
        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
        camera = DJSDKApplication.getCameraInstance();
        if(camera != null) {
            camera.setMode(SettingsDefinitions.CameraMode.RECORD_VIDEO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    if (error == null) {
                        Log.i(TAG, "Switch Camera Mode Succeeded");
                    } else {
                        Log.e(TAG, error.getDescription());
                    }
                }
            });
            camera.setFocusMode(SettingsDefinitions.FocusMode.AFC, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    Log.i(TAG, "Set focus Mode: " + (error == null ? "AFC success" : error.getDescription()));
                }
            });
        }
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateTitleBar();
            initPreviewer();
        }

    };
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        initPreviewer();
        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    protected void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        uninitPreviewer();
        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
            mReceiver=null;
        }
        super.onDestroy();
    }

    private void initPreviewer() {
        try {
            mProduct = DJSDKApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
        }
        if (mProduct == null || !mProduct.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!mProduct.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(mReceivedVideoDataCallBack);
            }
        }
    }

    private void uninitPreviewer() {
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(FPVActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTitleBar() {
        if(mConnectStatusTextView == null) return;
        boolean ret = false;
        BaseProduct product = DJSDKApplication.getProductInstance();
        if (product != null) {
            if(product.isConnected()) {
                mConnectStatusTextView.setText(DJSDKApplication.getProductInstance().getModel().getDisplayName() + " Connected");
                ret = true;
            } else {
                if(product instanceof Aircraft) {
                    Aircraft aircraft = (Aircraft)product;
                    if(aircraft.getRemoteController() != null) {
                    }
                    if(aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                        mConnectStatusTextView.setText("only RC Connected");
                        ret = true;
                    }
                }
            }
        }
        if(!ret) {
//            mConnectStatusTextView.setText("Disconnected");
        }
    }
}

