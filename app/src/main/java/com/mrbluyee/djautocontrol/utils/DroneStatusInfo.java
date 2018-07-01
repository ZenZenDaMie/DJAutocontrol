package com.mrbluyee.djautocontrol.utils;

public class DroneStatusInfo {
    private String connect_status,charge_status;
    private int charge,voltage,current;
    private float temperature;
    private double longitude,latitude;
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

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLatitude(){
        return latitude;
    }


    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public double getLongitude(){
        return longitude;
    }

    public void setVoltage(int vol) {
        this.voltage = vol;
    }

    public int getVoltage(){
        return voltage;
    }

    public void setCurrent(int cur) {
        this.current = cur;
    }

    public int getCurrent(){
        return current;
    }

    public String getCharge_status() {
        return charge_status;
    }

    public void setCharge_status(String cs) {
        this.charge_status = cs;
    }

}
