package com.mrbluyee.djautocontrol.application;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.amap.api.maps2d.model.LatLng;
import com.google.gson.Gson;
import com.mrbluyee.djautocontrol.activity.FollowmeActivity;
import com.mrbluyee.djautocontrol.utils.ChargeStationInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dji.thirdparty.okhttp3.FormBody;
import dji.thirdparty.okhttp3.MediaType;
import dji.thirdparty.okhttp3.OkHttpClient;
import dji.thirdparty.okhttp3.Request;
import dji.thirdparty.okhttp3.RequestBody;
import dji.thirdparty.okhttp3.Response;
import okhttp3.Call;

public class WebRequestApplication {
    public void Get_chargesite_gps_info(final Handler UIHandler){
        new Thread(new Runnable() {
            String  url = "http://139.196.138.204/tp5/public/station/returngaodegps";
            String result = null;
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Log.d("getgps","response.code()=="+response.code());
                        result = response.body().string();
                        Message msg = new Message();
                        Bundle b = new Bundle();// 存放数据
                        b.putString("getgps",result);
                        msg.setData(b);
                        UIHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public SparseArray<ChargeStationInfo> chargeStationInfoHandler(Bundle b)
    {
        SparseArray<ChargeStationInfo> stationInfos = new SparseArray<ChargeStationInfo>();
        JSONArray station_gps_jsonArray = null;
        String station_gps_data = b.getString("getgps");
        if(station_gps_data != null) {
            Log.d("MyHandler", station_gps_data);
            try {
                station_gps_jsonArray = new JSONArray(station_gps_data);
                for (int i = 0; i < station_gps_jsonArray.length(); i++) {
                    JSONObject jsonObject = station_gps_jsonArray.getJSONObject(i);
                    int station_id = jsonObject.getInt("stationid");
                    double station_lat = jsonObject.getDouble("lat");
                    double station_lon = jsonObject.getDouble("lon");
                    LatLng station_pos = new LatLng(station_lat, station_lon);
                    String station_create_time = jsonObject.getString("create_time");
                    String station_update_time = jsonObject.getString("update_time");
                    ChargeStationInfo stationInfo = new ChargeStationInfo();
                    stationInfo.setStationPos(station_pos);
                    stationInfo.setStation_create_time(station_create_time);
                    stationInfo.setStation_update_time(station_update_time);
                    stationInfos.append(station_id, stationInfo);
                    Log.d("MyHandler", "append station:"+ station_id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return stationInfos;
    }


    public void Get_chargesite_info(final Handler UIHandler,final String stationid){
        new Thread(new Runnable() {
            //String  url = "http://139.196.138.204/tp5/public/station/readstation?stationid="+ stationid;\
            String  url = "http://139.196.138.204/tp5/public/station/readstation";
            String result = null;
            @Override
            public void run() {
                try {
                    OkHttpUtils
                            .get()
                            .url(url)
                            .addParams("stationid", ""+stationid)
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {

                                    Message msg = new Message();
                                    Bundle b = new Bundle();// 存放数据
                                    b.putString("IntentErr","1");
                                    msg.setData(b);
                                    UIHandler.sendMessage(msg);
                                }

                                @Override
                                public void onResponse(String _response) {
                                    Gson gson = new Gson();
                                    StationBean log_respon = gson.fromJson(_response, StationBean.class);
                                    if(log_respon.getStationid()!="0")
                                    {
                                        Message msg = new Message();
                                        Bundle b = new Bundle();// 存放数据
                                        b.putString("stationstatus",log_respon.getStationstatus());
                                        b.putString("stationid",log_respon.getStationid());
                                        b.putString("stationpower",log_respon.getPower());
                                        b.putString("updatetime",log_respon.getUpdate_time());
                                        b.putString("uavid",log_respon.getUavid());
                                        msg.setData(b);
                                        UIHandler.sendMessage(msg);
                                    }
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void Get_chargesite_drone_info(final Handler UIHandler){
        new Thread(new Runnable() {
            String  url = "http://139.196.138.204/tp5/public/station/getuav?uavid=saas&stationid=112130";
            String result = null;
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {
                        Log.d("getdroneinfo","response.code()=="+response.code());
                        result = response.body().string();
                        JSONObject object = new JSONObject(result);
                        if(object.has("uavid")){
                            String uavid = object.getString("uavid");
                            Message msg = new Message();
                            Bundle b = new Bundle();// 存放数据
                            b.putString("uavid", uavid);
                            msg.setData(b);
                            UIHandler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void Post_drone_info(final String droneinfo) {
        new Thread(new Runnable() {
            String url = "http://139.196.138.204/tp5/public/station/updateuav";
            String result = null;

            @Override
            public void run() {
                try {
                    OkHttpUtils
                            .postString()
                            .url(url)
                            .content(droneinfo)
                            .mediaType(okhttp3.MediaType.parse("application/x-www-form-urlencoded"))
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                    Log.e("MyHttpHandler", "post Error: " + e.getMessage());
                                }

                                @Override
                                public void onResponse(String _response) {
                                    Log.i("MyHttpHandler", "post response: " + _response);
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
