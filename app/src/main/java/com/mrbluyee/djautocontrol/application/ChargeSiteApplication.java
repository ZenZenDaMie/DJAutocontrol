package com.mrbluyee.djautocontrol.application;

import android.support.annotation.Nullable;

public class ChargeSiteApplication {
    @Nullable
    public static String get_chargesite_gps_info(){
        String  url = "http://139.196.138.204/tp5/public/station/returngaodegps";
        String web_data = WebApplication.http_get(url);
        if(web_data != null){
            return web_data;
        }
        return null;
    }
}
