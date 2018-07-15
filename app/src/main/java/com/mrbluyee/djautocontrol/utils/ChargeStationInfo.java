package com.mrbluyee.djautocontrol.utils;

import android.support.annotation.NonNull;

import com.amap.api.maps2d.model.LatLng;

public class ChargeStationInfo {
    private LatLng station_pos;
    private String stationid;
    private String uav_pow;
    private String uavid;
    private String stationstatus;
    private String stationcontrol;
    private String station_create_time;
    private String station_update_time;

    public String getStationId() {
        return stationid;
    }

    public void setStationId(String stationid) {
        this.stationid = stationid;
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

    public void setDronePow(String uav_pow) {
        this.uav_pow = uav_pow;
    }

    public String getDronePow(){
        return uav_pow;
    }

    public void setStationStatus(String stationstatus) {
        this.stationstatus = stationstatus;
    }

    public String getStationStatus(){
        return stationstatus;
   }

    public void setStationcontrol(String stationcontrol){
        this.stationcontrol = stationcontrol;
    }

    public String getStationcontrol() {
        return stationcontrol;
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
