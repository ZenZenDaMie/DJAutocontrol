package com.mrbluyee.djautocontrol.application;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebApplication {
    //从流中读取数据
    public static byte[] read(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer,0,len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    public static String http_get(String path){
        String get_data = null;
        URL url = null;
        InputStream in = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                in = connection.getInputStream();
                byte[] data = read(in);
                get_data = new String(data, "UTF-8");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return get_data;
    }

    public static String http_post(String path,String post_data){
        String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            DataOutputStream dop = new DataOutputStream(
                    connection.getOutputStream());
            dop.writeBytes(post_data);
            dop.flush();
            dop.close();

            in = connection.getInputStream();
            byte[] data = read(in);
            result = new String(data, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
