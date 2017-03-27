package com.example.user.mtu_size;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by car on 2017/3/8.
 */

public class ECG_DATA implements Serializable{

    private int deviceid;
    private double time;
    private double data;

    public ECG_DATA() {
    }

    public ECG_DATA(int deviceid, double time, double data) {

        this.deviceid = deviceid;
        this.time = time;
        this.data = data;

    }

    public int getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public JSONObject toJsonObject(){
        JSONObject json = new JSONObject();

        try {
            json.put("deviceid", deviceid);
            json.put("time", time);
            json.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
