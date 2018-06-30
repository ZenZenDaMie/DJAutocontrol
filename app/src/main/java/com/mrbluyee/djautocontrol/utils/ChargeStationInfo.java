package com.mrbluyee.djautocontrol.utils;

import android.support.annotation.NonNull;

import com.amap.api.maps2d.model.LatLng;

public class ChargeStationInfo {
    private LatLng station_pos;
    private int uav_pow;
    private String uavid;
    private int stationstatus;
    private String station_create_time;
    private String station_update_time;
    private String connect_status;
    private int charge;
    private float temperature;

    public void setTemperature(float tem) {
        this.temperature = tem;
    }

    public float getTemperature(){
        return temperature;
    }

    public void setCharge(int ch) {
        this.charge = ch;
    }

    public int getCharge(){
        return charge;
    }

    public String getConnect_status() {
        return connect_status;
    }

    public void setConnect_status(String con) {
        this.connect_status = con;
    }

    public LatLng getStationPos() {
        return station_pos;
    }

    public void setStationPos(LatLng station_pos){
        this.station_pos = station_pos;
    }

    public void setDroneId(String uavid) {
        this.uavid = uavid;
    }

    public String getDroneId() {
        return uavid;
    }

    public void setDronePow(int uav_pow) {
        this.uav_pow = uav_pow;
    }

    public int getDronePow(){
        return uav_pow;
    }

    public void setStationStatus(int stationstatus) {
        this.stationstatus = stationstatus;
    }

   public int getStationStatus(){
        return stationstatus;
   }

   public void setStation_create_time(String station_create_time){
        this.station_create_time = station_create_time;
   }

    public String getStation_create_time() {
        return station_create_time;
    }

    public void setStation_update_time(String station_update_time){
        this.station_update_time = station_update_time;
    }

    public String getStation_update_time() {
        return station_update_time;
    }

}
