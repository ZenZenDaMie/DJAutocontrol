package com.mrbluyee.djautocontrol.application;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.amap.api.maps2d.model.LatLng;
import com.mrbluyee.djautocontrol.activity.FollowmeActivity;
import com.mrbluyee.djautocontrol.utils.ChargeStationInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dji.thirdparty.okhttp3.OkHttpClient;
import dji.thirdparty.okhttp3.Request;
import dji.thirdparty.okhttp3.Response;

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
                        Log.d("getgps","response.message()=="+response.message());
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

    public void chargeStationInfoHandler(Bundle b,SparseArray<ChargeStationInfo> stationInfos)
    {
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
                    if(stationInfos.indexOfKey(station_id) == -1){    //no data
                        ChargeStationInfo stationInfo = new ChargeStationInfo();
                        stationInfo.setStationPos(station_pos);
                        stationInfo.setStation_create_time(station_create_time);
                        stationInfo.setStation_update_time(station_update_time);
                        stationInfos.append(station_id, stationInfo);
                        Log.d("MyHandler", "append station:"+ station_id);
                    }
                    else { //update data
                        ChargeStationInfo savedstationInfo = stationInfos.valueAt(station_id);
                        boolean info_changed = false;
                        if((savedstationInfo.getStationPos().longitude != station_pos.longitude)&&(savedstationInfo.getStationPos().latitude != station_pos.latitude)) {
                            savedstationInfo.setStationPos(station_pos);
                            info_changed = true;
                        }
                        if(!savedstationInfo.getStation_create_time().equals(station_create_time)) {
                            savedstationInfo.setStation_create_time(station_create_time);
                            info_changed = true;
                        }
                        if(!savedstationInfo.getStation_update_time().equals(station_update_time)) {
                            savedstationInfo.setStation_update_time(station_update_time);
                            info_changed = true;
                        }
                        if(info_changed) {
                            stationInfos.put(station_id, savedstationInfo);
                            Log.d("MyHandler", "modify station:"+ station_id);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Get_chargesite_info(final Handler UIHandler,final int stationid){
        new Thread(new Runnable() {
            String  url = "http://139.196.138.204/tp5/public/station/readstation?stationid="+ stationid;
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
                        Log.d("getinfo","response.code()=="+response.code());
                        Log.d("getinfo","response.message()=="+response.message());
                        result = response.body().string();
                        Message msg = new Message();
                        Bundle b = new Bundle();// 存放数据
                        b.putString("getinfo",result);
                        msg.setData(b);
                        UIHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
