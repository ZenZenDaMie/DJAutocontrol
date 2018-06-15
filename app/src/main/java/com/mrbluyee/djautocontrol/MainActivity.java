package com.mrbluyee.djautocontrol;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener{
    private Button drone_status_btn,camera_fpv_btn,remote_control_btn,auto_charge_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI(){
        drone_status_btn = (Button) findViewById(R.id.drone_status_button);
        camera_fpv_btn = (Button) findViewById(R.id.camera_fpv_button);
        remote_control_btn = (Button) findViewById(R.id.remote_control_button);
        auto_charge_btn = (Button) findViewById(R.id.auto_charge_button);

        drone_status_btn.setOnClickListener(this);
        camera_fpv_btn.setOnClickListener(this);
        remote_control_btn.setOnClickListener(this);
        auto_charge_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.drone_status_button:{
                break;
            }
            case R.id.camera_fpv_button:{
                Intent intent = new Intent(this, FPVActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.remote_control_button:{

                break;
            }
            case R.id.auto_charge_button:{

                break;
            }
            default:
                break;
        }
    }

}
