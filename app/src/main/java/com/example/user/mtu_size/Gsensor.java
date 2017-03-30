package com.example.user.mtu_size;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by car on 2017/3/26.
 */

public class Gsensor implements Serializable {

    private int deviceid;
    private double time;
    private double axis_X;
    private double axis_Y;
    private double axis_Z;
    private double comVec;
    private double axis[] = new double[3];

    public Gsensor() {
    }

    public Gsensor(double[] axis, double comVec, double time, int deviceid) {
        this.axis = axis;
        this.comVec = comVec;
        this.time = time;
        this.deviceid = deviceid;
    }

    public Gsensor(double comVec, int deviceid, double time, double axis_X, double axis_Y, double axis_Z) {
        this.comVec = comVec;
        this.deviceid = deviceid;
        this.time = time;
        this.axis_X = axis_X;
        this.axis_Y = axis_Y;
        this.axis_Z = axis_Z;
    }

    public int getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(int deviceid) {
        this.deviceid = deviceid;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getComVec() {
        return comVec;
    }

    public void setComVec(double comVec) {
        this.comVec = comVec;
    }

    public double getAxis_Z() {
        return axis_Z;
    }

    public void setAxis_Z(double axis_Z) {
        this.axis_Z = axis_Z;
    }

    public double getAxis_Y() {
        return axis_Y;
    }

    public void setAxis_Y(double axis_Y) {
        this.axis_Y = axis_Y;
    }

    public double getAxis_X() {
        return axis_X;
    }

    public void setAxis_X(double axis_X) {
        this.axis_X = axis_X;
    }

    public double[] getAxis(double[] axis) {
        return this.axis;
    }

    public void setAxis(double[] axis) {
        this.axis = axis;
    }

    public JSONObject toJsonObject(){
        JSONObject json = new JSONObject();

        try {
            json.put("gdeviceid", deviceid);
            json.put("gtime", time);
            //json.put("axisX", axis_X);
            //json.put("axisY", axis_Y);
            //json.put("axisZ", axis_Z);
            json.put("axisX", axis[0]);
            json.put("axisY", axis[1]);
            json.put("axisZ", axis[2]);
            json.put("comVec", comVec);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
