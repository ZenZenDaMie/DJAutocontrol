package com.mrbluyee.djautocontrol.application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mrbluyee.djautocontrol.R;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

//import com.scwang.smartrefresh.layout.api.RefreshLayout;

/**
 * Created by rsj on 2018/1/12.
 */

public class StationStatusActivity extends AppCompatActivity {
    private int i = 0;
    private int TIME = 1000;

    public Boolean root;
    public Boolean admin;
    public Boolean user;
    public String name;
    public String stationid;
    public String longitude;
    public String latitude;
    private TextView readlog;

    public TextView text1;
    public TextView text2;
    public TextView text3;
    public TextView text4;
    public TextView textid;
    public TextView textlon;
    public TextView textlat;
//    public String stationid="112130";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationstatus);
        textid=(TextView)findViewById(R.id.textid);
        text1=(TextView)findViewById(R.id.text1);
        text2=(TextView)findViewById(R.id.text2);
        text3=(TextView)findViewById(R.id.text3);
        text4=(TextView)findViewById(R.id.text4);
        textlon=(TextView)findViewById(R.id.textlon);
        textlat=(TextView)findViewById(R.id.textlat);
        readlog=(TextView)findViewById(R.id.readlog);
        Bundle bundle = this.getIntent().getExtras();
/*
        root = bundle.getBoolean("root");
        admin = bundle.getBoolean("admin");
        user = bundle.getBoolean("user");
        name=bundle.getString("name");
        */
        stationid=bundle.getString("stationid");
        longitude=bundle.getString("longitude");
        latitude=bundle.getString("latitude");
//        Toast.makeText(getApplicationContext(),""+longitude+" "+latitude,Toast.LENGTH_LONG).show();
        request();

        RefreshLayout refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Toast.makeText(getApplicationContext(), "刷新成功", Toast.LENGTH_SHORT).show();//提示用户登录失败
//                text1.setText("充电站状态：");
//                text2.setText("无人机电量：");
//                text3.setText("？？？？");
//                text4.setText("无人机ID：");
                request();
                RefreshLayout refreshLayout = findViewById(R.id.refreshLayout);
                refreshlayout.finishRefresh(50/*,false*/);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
//                Toast.makeText(getApplicationContext(), "11111", Toast.LENGTH_SHORT).show();//提示用户登录失败
                refreshlayout.finishLoadmore(50/*,false*/);//传入false表示加载失败
            }
        });
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        timer.schedule(task, 1000, 1000); // 1s后执行task,经过1s再次执行


        //状态栏透明和间距处理
        StatusBarUtil.immersive(this);
        StatusBarUtil.setPaddingSmart(this, toolbar);
        StatusBarUtil.setPaddingSmart(this, findViewById(R.id.profile));
        StatusBarUtil.setPaddingSmart(this, findViewById(R.id.blurview));

        readlog.setOnClickListener(new View.OnClickListener()//侦听登录点击事件
        {
            public void onClick(View v) {//验证用户名密码是否符合要求
                /*
                if(admin.equals(true)){
                Intent intent = new Intent(StationStatusActivity.this, ClassicsStyleActivity.class);
//                用Bundle携带数据
                Bundle bundle = new Bundle();
                //传递name参数为tinyphp

                bundle.putString("stationid", stationid);
                intent.putExtras(bundle);

                startActivity(intent);}
                else {
                    Toast.makeText(getApplicationContext(), "权限不足", Toast.LENGTH_SHORT).show();
                }
                */
            }
        });
    }
    private void request(){
        String url = "http://139.196.138.204/tp5/public/station/readstation";
        OkHttpUtils
                .get()
                .url(url)
                .addParams("stationid", stationid)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        Toast.makeText(getApplicationContext(), "连接错误，请查看网络连接", Toast.LENGTH_SHORT).show();
                        //                        ToastUtil.show("连接错误，请查看网络连接");
                    }

                    @Override
                    public void onResponse(String _response) {
                        Gson gson = new Gson();
                        StationBean log_respon = gson.fromJson(_response, StationBean.class);
                        if(log_respon.getStationid()!="0")
                        {

                            if(log_respon.getStationstatus().equals("0"))
                                text1.setText("充电站状态： 空闲");
                            else if(log_respon.getStationstatus().equals("1"))
                                text1.setText("充电站状态： 正在充电");
                            else if(log_respon.getStationstatus().equals("2"))
                                text1.setText("充电站状态： 已充满");

                            textid.setText("充电站ID："+log_respon.getStationid());
                            text2.setText("无人机电量："+log_respon.getPower());
                            if(log_respon.getStationstatus().equals("0"))
                                text2.setText("无人机电量：");
                            text3.setText("更新时间："+log_respon.getUpdate_time());
                            text4.setText("无人机ID："+log_respon.getUavid());
                            if(log_respon.getUavid().equals("00000000"))
                                text4.setText("无人机ID：");
                            textlon.setText("经度："+longitude);
                            textlat.setText("纬度："+latitude);
                        }
                    }
                });
    }
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                request();
//                Toast.makeText(getApplicationContext(),Integer.toString(i++),Toast.LENGTH_SHORT).show();
                //                tvShow.setText(Integer.toString(i++));
            }
            super.handleMessage(msg);
        };
    };
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {

        @Override
        public void run() {
            // 需要做的事:发送消息
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    };
}
