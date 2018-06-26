package com.mrbluyee.djautocontrol.activity;

import dji.common.error.DJIError;
import dji.common.mission.tapfly.TapFlyExecutionState;
import dji.common.mission.tapfly.TapFlyMission;
import dji.common.mission.tapfly.TapFlyMissionState;
import dji.common.mission.tapfly.TapFlyMode;
import dji.common.util.CommonCallbacks;
import dji.internal.geofeature.flyforbid.Utils;
import dji.sdk.mission.tapfly.TapFlyMissionEvent;
import dji.sdk.mission.tapfly.TapFlyMissionOperator;
import dji.sdk.mission.tapfly.TapFlyMissionOperatorListener;
import dji.sdk.sdkmanager.DJISDKManager;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SlidingDrawer;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mrbluyee.djautocontrol.application.PictureHandle;
import com.mrbluyee.djautocontrol.utils.StringHandleUtil;
import com.mrbluyee.djautocontrol.application.FPVActivity;
import com.mrbluyee.djautocontrol.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Rect;

public class TapflyActivity extends FPVActivity implements TextureView.SurfaceTextureListener, View.OnClickListener,View.OnTouchListener {
    private static final String TAG = TapflyActivity.class.getName();
    private TapFlyMission mTapFlyMission;

    private ImageButton mPushDrawerIb;
    private SlidingDrawer mPushDrawerSd;
    private Button mStartBtn;
    private ImageButton mStopBtn;
    private TextView mPushTv;
    private RelativeLayout mBgLayout;
    private ImageView mRstPointIv;
    private TextView mAssisTv;
    private Switch mAssisSw;
    private TextView mSpeedTv;
    private SeekBar mSpeedSb;
    private MyHandler myHandler;
    private ImageView mTrackingImage;
    private Rect[] targetsArray = null;
    private PictureHandle picturehandle = new PictureHandle(this);

    private TapFlyMissionOperator getTapFlyOperator() {
        return DJISDKManager.getInstance().getMissionControl().getTapFlyMissionOperator();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_tapfly);
        super.onCreate(savedInstanceState);
        initUI();
        getTapFlyOperator().addListener(new TapFlyMissionOperatorListener() {
            @Override
            public void onUpdate(@Nullable TapFlyMissionEvent aggregation) {
                TapFlyExecutionState executionState = aggregation.getExecutionState();
                if (executionState != null){
                    showPointByTapFlyPoint(executionState.getImageLocation(), mRstPointIv);
                }

                StringBuffer sb = new StringBuffer();
                String errorInformation = (aggregation.getError() == null ? "null" : aggregation.getError().getDescription()) + "\n";
                String currentState = aggregation.getCurrentState() == null ? "null" : aggregation.getCurrentState().getName();
                String previousState = aggregation.getPreviousState() == null ? "null" : aggregation.getPreviousState().getName();
                StringHandleUtil.addLineToSB(sb, "CurrentState: ", currentState);
                StringHandleUtil.addLineToSB(sb, "PreviousState: ", previousState);
                StringHandleUtil.addLineToSB(sb, "Error:", errorInformation);

                TapFlyExecutionState progressState = aggregation.getExecutionState();

                if (progressState != null) {
                    StringHandleUtil.addLineToSB(sb, "Heading: ", progressState.getRelativeHeading());
                    StringHandleUtil.addLineToSB(sb, "PointX: ", progressState.getImageLocation().x);
                    StringHandleUtil.addLineToSB(sb, "PointY: ", progressState.getImageLocation().y);
                    StringHandleUtil.addLineToSB(sb, "BypassDirection: ", progressState.getBypassDirection().name());
                    StringHandleUtil.addLineToSB(sb, "VectorX: ", progressState.getDirection().getX());
                    StringHandleUtil.addLineToSB(sb, "VectorY: ", progressState.getDirection().getY());
                    StringHandleUtil.addLineToSB(sb, "VectorZ: ", progressState.getDirection().getZ());
                    setResultToText(sb.toString());
                }

                TapFlyMissionState missionState = aggregation.getCurrentState();
                if (!((missionState == TapFlyMissionState.EXECUTING) || (missionState == TapFlyMissionState.EXECUTION_PAUSED)
                        || (missionState == TapFlyMissionState.EXECUTION_RESETTING))){
                    setVisible(mRstPointIv, false);
                    setVisible(mStopBtn, false);
                }else
                {
                    setVisible(mStopBtn, true);
                    setVisible(mStartBtn, false);
                }
            }
        });
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
                TapflyActivity.this.runOnUiThread(new Runnable() {
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
                TapflyActivity.this.runOnUiThread(new Runnable() {
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
        initTapFlyMission();
    }

    @Override
    protected void onDestroy() {

        if(mCodecManager != null){
            mCodecManager.destroyCodec();
        }

        super.onDestroy();
    }

    /**
     * @Description : RETURN BTN RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            picturehandle.onManagerConnected(status);
        }
    };

    private void setResultToToast(final String string) {
        TapflyActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TapflyActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResultToText(final String string) {
        if (mPushTv == null) {
            setResultToToast("Push info tv has not be init...");
        }
        TapflyActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPushTv.setText(string);
            }
        });
    }

    private void setVisible(final View v, final boolean visible) {
        if (v == null) return;
        TapflyActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                v.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private void initUI() {
        mPushDrawerIb = (ImageButton)findViewById(R.id.pointing_drawer_control_ib);
        mPushDrawerSd = (SlidingDrawer)findViewById(R.id.pointing_drawer_sd);
        mStartBtn = (Button)findViewById(R.id.pointing_start_btn);
        mStopBtn = (ImageButton)findViewById(R.id.pointing_stop_btn);
        mPushTv = (TextView)findViewById(R.id.pointing_push_tv);
        mBgLayout = (RelativeLayout)findViewById(R.id.pointing_bg_layout);
        mRstPointIv = (ImageView)findViewById(R.id.pointing_rst_point_iv);
        mAssisTv = (TextView)findViewById(R.id.pointing_assistant_tv);
        mAssisSw = (Switch)findViewById(R.id.pointing_assistant_sw);
        mSpeedTv = (TextView)findViewById(R.id.pointing_speed_tv);
        mSpeedSb = (SeekBar)findViewById(R.id.pointing_speed_sb);
        mTrackingImage = (ImageView) findViewById(R.id.tapfly_tracking_send_rect);

        mPushDrawerIb.setOnClickListener(this);
        mStartBtn.setOnClickListener(this);
        mStopBtn.setOnClickListener(this);
        mBgLayout.setOnTouchListener(this);
        mSpeedSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSpeedTv.setText(progress + 1 + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getTapFlyOperator().setAutoFlightSpeed(getSpeed(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError error) {
                        setResultToToast(error == null ? "Set Auto Flight Speed Success" : error.getDescription());
                    }
                });
            }
        });
    }

    private void initTapFlyMission() {
        mTapFlyMission = new TapFlyMission();
        mTapFlyMission.isHorizontalObstacleAvoidanceEnabled = mAssisSw.isChecked();
        mTapFlyMission.tapFlyMode = TapFlyMode.FORWARD;
    }

    private PointF getTapFlyPoint(View iv) {
        if (iv == null) return null;
        View parent = (View)iv.getParent();
        float centerX = iv.getLeft() + iv.getX()  + ((float)iv.getWidth()) / 2;
        float centerY = iv.getTop() + iv.getY() + ((float)iv.getHeight()) / 2;
        centerX = centerX < 0 ? 0 : centerX;
        centerX = centerX > parent.getWidth() ? parent.getWidth() : centerX;
        centerY = centerY < 0 ? 0 : centerY;
        centerY = centerY > parent.getHeight() ? parent.getHeight() : centerY;

        return new PointF(centerX / parent.getWidth(), centerY / parent.getHeight());
    }

    private void showPointByTapFlyPoint(final PointF point, final ImageView iv) {
        if (point == null || iv == null) {
            return;
        }
        final View parent = (View)iv.getParent();
        TapflyActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                iv.setX(point.x * parent.getWidth() - iv.getWidth() / 2);
                iv.setY(point.y * parent.getHeight() - iv.getHeight() / 2);
                iv.setVisibility(View.VISIBLE);
                iv.requestLayout();
            }
        });
    }

    private float getSpeed() {
        if (mSpeedSb == null) return Float.NaN;
        return mSpeedSb.getProgress() + 1;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.pointing_bg_layout) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (mTapFlyMission != null) {
                        mStartBtn.setVisibility(View.VISIBLE);
                        mStartBtn.setX(event.getX() - mStartBtn.getWidth() / 2);
                        mStartBtn.setY(event.getY() - mStartBtn.getHeight() / 2);
                        mStartBtn.requestLayout();
                        mTapFlyMission.target = getTapFlyPoint(mStartBtn);
                    } else {
                        setResultToToast("TapFlyMission is null");
                    }
                    break;

                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.pointing_drawer_control_ib) {
            if (mPushDrawerSd.isOpened()) {
                mPushDrawerSd.animateClose();
            } else {
                mPushDrawerSd.animateOpen();
            }
            return;
        }
        if (getTapFlyOperator() != null) {
            switch (v.getId()) {
                case R.id.pointing_start_btn:
                    getTapFlyOperator().startMission(mTapFlyMission, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError error) {
                            setResultToToast(error == null ? "Start Mission Successfully" : error.getDescription());
                            if (error == null){
                                setVisible(mStartBtn, false);
                            }
                        }
                    });
                    break;
                case R.id.pointing_stop_btn:
                    getTapFlyOperator().stopMission(new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError error) {
                            setResultToToast(error == null ? "Stop Mission Successfully" : error.getDescription());
                        }
                    });
                    break;
                default:
                    break;
            }
        } else {
            setResultToToast("TapFlyMission Operator is null");
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        super.onSurfaceTextureUpdated(surface);
        Bitmap bitmap = mVideoSurface.getBitmap();
        picturehandle.new Picture_Match(bitmap,myHandler).begin();
    }
}
