package com.mrbluyee.djautocontrol.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.useraccount.UserAccountManager;

import com.mrbluyee.djautocontrol.R;

public class MainActivity extends Activity implements OnClickListener{
    private TextView drone_status_btn;
    private TextView camera_fpv_btn;
    private TextView remote_control_btn;
    private TextView gps_track_btn;
    private TextView gps_route_btn;
    private TextView static_track_btn;
    private TextView moving_track_btn;
    private TextView site_landing_btn;
    private TextView automatic_handle_btn;
    private static final String TAG = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }
    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        loginAccount();
    }
    private void initUI(){
        drone_status_btn = (TextView) findViewById(R.id.drone_status_button);
        camera_fpv_btn = (TextView) findViewById(R.id.camera_fpv_button);
        remote_control_btn = (TextView) findViewById(R.id.remote_control_button);
        gps_track_btn = (TextView) findViewById(R.id.gps_track_button);
        gps_route_btn = (TextView) findViewById(R.id.gps_route_button);
        static_track_btn = (TextView) findViewById(R.id.static_track_button);
        moving_track_btn = (TextView) findViewById(R.id.moving_track_button);
        site_landing_btn = (TextView) findViewById(R.id.site_landing_button);
        automatic_handle_btn = (TextView) findViewById(R.id.automatic_handle_button);

        drone_status_btn.setOnClickListener(this);
        camera_fpv_btn.setOnClickListener(this);
        remote_control_btn.setOnClickListener(this);
        gps_track_btn .setOnClickListener(this);
        gps_route_btn.setOnClickListener(this);
        static_track_btn.setOnClickListener(this);
        moving_track_btn.setOnClickListener(this);
        site_landing_btn.setOnClickListener(this);
        automatic_handle_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.drone_status_button:{
                Intent intent = new Intent(this, PushDataActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.camera_fpv_button:{
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.remote_control_button:{
                Intent intent = new Intent(this, RemoteControlActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.gps_track_button:{
                Intent intent = new Intent(this, FollowmeActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.gps_route_button:{
                Intent intent = new Intent(this, WaypointActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.static_track_button:{
                Intent intent = new Intent(this, TapflyActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.moving_track_button:{
                Intent intent = new Intent(this, ActiveTrackActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.site_landing_button:{
                Intent intent = new Intent(this, SiteLandingActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.automatic_handle_button:{

                break;
            }
            default:
                break;
        }
    }
    private void loginAccount(){
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        showToast("Login Error:" + error.getDescription());
                    }
                });
    }
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
