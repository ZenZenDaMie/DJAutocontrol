package com.mrbluyee.djautocontrol;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.useraccount.UserAccountManager;


public class MainActivity extends Activity implements OnClickListener{
    private Button drone_status_btn,camera_fpv_btn,remote_control_btn,gps_location_btn,picture_location_btn,site_landing_btn,automatic_handle_btn;
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
        drone_status_btn = (Button) findViewById(R.id.drone_status_button);
        camera_fpv_btn = (Button) findViewById(R.id.camera_fpv_button);
        remote_control_btn = (Button) findViewById(R.id.remote_control_button);
        gps_location_btn = (Button) findViewById(R.id.gps_location_button);
        picture_location_btn = (Button) findViewById(R.id.picture_location_button);
        site_landing_btn = (Button) findViewById(R.id.site_landing_button);
        automatic_handle_btn = (Button) findViewById(R.id.automatic_handle_button);

        drone_status_btn.setOnClickListener(this);
        camera_fpv_btn.setOnClickListener(this);
        remote_control_btn.setOnClickListener(this);
        gps_location_btn.setOnClickListener(this);
        picture_location_btn.setOnClickListener(this);
        site_landing_btn.setOnClickListener(this);
        automatic_handle_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.drone_status_button:{
                break;
            }
            case R.id.camera_fpv_button:{
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.remote_control_button:{

                break;
            }
            case R.id.gps_location_button:{
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.picture_location_button:{

                break;
            }
            case R.id.site_landing_button:{

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
