package com.mrbluyee.djautocontrol.application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.mrbluyee.djautocontrol.R;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;


import java.util.Timer;
import java.util.TimerTask;




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
    public String stationstatus="";
    private TextView btn_set_target_station;

    public TextView text1;
    public TextView text2;
    public TextView text3;
    public TextView text4;
    public TextView textid;
    public TextView textlon;
    public TextView textlat;
    private WebRequestApplication webrequest = new WebRequestApplication();

    public MyHandler myHandler;

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
        btn_set_target_station=(TextView)findViewById(R.id.set_target_station);
        Bundle bundle = this.getIntent().getExtras();

        stationid=bundle.getString("stationid");
        longitude=bundle.getString("longitude");
        latitude=bundle.getString("latitude");
        myHandler = new MyHandler();
//        Toast.makeText(getApplicationContext(),""+longitude+" "+latitude,Toast.LENGTH_LONG).show();
        webrequest.Get_chargesite_info(myHandler,stationid);
        //request();

        RefreshLayout refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Toast.makeText(getApplicationContext(), "刷新成功", Toast.LENGTH_SHORT).show();//提示用户登录失败
                text1.setText("充电站状态：");
                text2.setText("无人机电量：");
                text3.setText("？？？？");
                text4.setText("无人机ID：");
                //request();
                webrequest.Get_chargesite_info(myHandler,stationid);
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
        btn_set_target_station.setOnClickListener(new View.OnClickListener()//侦听登录点击事件
        {
            public void onClick(View v) {//设置目标站点
                if(stationstatus.equals("0")) {
                    //充电站空闲
                }
                    //三要素
                    //stationid
                    //longitude
                    //latitude

                /*
                Intent intent = new Intent(StationStatusActivity.this, .class);
                //                用Bundle携带数据
                Bundle bundle = new Bundle();
                //传递name参数为tinyphp

                bundle.putString("stationid", stationid);
                intent.putExtras(bundle);

                startActivity(intent);*/
            }
        });

    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //request();
                webrequest.Get_chargesite_info(myHandler,stationid);

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
            //Toast.makeText(getApplicationContext(),"启动",Toast.LENGTH_LONG).show();
            Bundle b = msg.getData();
            if(b.getString("stationstatus")!=null)
                stationstatus=b.getString("stationstatus");
            if (b.getString("stationid")!=null && b.getString("stationid") != "0") {
                if (b.getString("stationstatus").equals("0"))
                    text1.setText("充电站状态： 空闲");
                else if (b.getString("stationstatus").equals("1"))
                    text1.setText("充电站状态： 正在充电");
                else if (b.getString("stationstatus").equals("2"))
                    text1.setText("充电站状态： 已充满");
                textid.setText("充电站ID：" + b.get("stationid"));
                text2.setText("无人机电量：" + b.get("stationpower"));
                if (b.getString("stationstatus").equals("0"))
                    text2.setText("无人机电量：");
                text3.setText("更新时间：" + b.getString("updatetime"));
                text4.setText("无人机ID：" + b.getString("uavid"));
                if (b.getString("uavid").equals("00000000"))
                    text4.setText("无人机ID：");
                textlon.setText("经度：" + longitude);
                textlat.setText("纬度：" + latitude);
            }

            if(b.getString("IntentErr")!=null && b.getString("IntentErr").equals("1"))
            {
                Toast.makeText(getApplicationContext(),"网络错误，请重新连结",Toast.LENGTH_LONG).show();
            }

        }
    }
}
